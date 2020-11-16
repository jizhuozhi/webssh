package cn.mgdream.webssh.core

import cn.mgdream.webssh.core.EventType.CONNECT
import cn.mgdream.webssh.core.EventType.DATA
import com.fasterxml.jackson.databind.ObjectMapper
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.Session
import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.io.InputStream
import java.io.OutputStream
import java.lang.System.currentTimeMillis
import java.util.concurrent.Executors

open class WebSshEventHandler(val objectMapper: ObjectMapper) {

    private val executorService = Executors.newFixedThreadPool(Int.MAX_VALUE)

    companion object {
        private const val timeout = 60 * 1000L
        private val logger = LoggerFactory.getLogger(WebSshEventHandler::class.java)
    }

    fun handleEvent(event: Event, session: WebSocketSession) {
        when (event.type) {
            EventType.COMMAND -> handleCommandEvent(event, session)
            CONNECT -> handleConnectEvent(event, session)
            EventType.DATA -> handleDataEvent(event, session)
            EventType.INTERCEPT -> handleInterceptEvent(event, session)
            EventType.NULL -> handleNullEvent(event, session)
            EventType.RESIZE -> handleResizeEvent(event, session)
        }
    }

    open fun handleCommandEvent(event: Event, session: WebSocketSession) {
        val payload = event.payload as String
        val outputStream = session.attributes["jSchOutputStream"] as OutputStream
        outputStream.write(payload.toByteArray(Charsets.UTF_8))
        outputStream.write('\r'.toInt())
        outputStream.flush()
    }

    open fun handleConnectEvent(event: Event, session: WebSocketSession) {
        val jSchSession = session.attributes["jSchSession"] as Session
        executorService.submit { startConnecting(session, jSchSession) }
    }

    open fun handleDataEvent(event: Event, session: WebSocketSession) {
        val payload = event.payload as String
        val outputStream = session.attributes["jSchOutputStream"] as OutputStream
        outputStream.write(payload.toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    open fun handleInterceptEvent(event: Event, session: WebSocketSession) {
        val outputStream = session.attributes["jSchOutputStream"] as OutputStream
        outputStream.write('\u0003'.toInt())
        outputStream.flush()
    }

    open fun handleNullEvent(event: Event, session: WebSocketSession) {
        val outputStream = session.attributes["jSchOutputStream"] as OutputStream
        outputStream.write('\r'.toInt())
        outputStream.flush()
    }

    open fun handleResizeEvent(event: Event, session: WebSocketSession) {
        val jSchChannel = session.attributes["jSchChannel"] as ChannelShell
        val payload = event.payload as Map<*, *>
        val cols = payload["cols"].toString().toInt()
        val rows = payload["rows"].toString().toInt()
        jSchChannel.setPtySize(cols, rows, cols * 8, rows * 8)
    }

    private fun startConnecting(session: WebSocketSession, jSchSession: Session) {
        jSchSession.connect()
        val jSchChannel = jSchSession.openChannel("shell")
        jSchChannel.connect()
        val jSchInputStream = jSchChannel.inputStream
        val jSchOutputStream = jSchChannel.outputStream
        session.attributes["jSchSession"] = jSchSession
        session.attributes["jSchChannel"] = jSchChannel
        session.attributes["jSchInputStream"] = jSchInputStream
        session.attributes["jSchOutputStream"] = jSchOutputStream
        session.sendMessage(
            TextMessage(objectMapper.writeValueAsString(Event(CONNECT)))
        )
        // Start transferring data after successful connection
        executorService.submit { transferData(session, jSchSession) }
        // Start watchdog after successful connection
        executorService.submit { dataTransmissionWatchdog(session, jSchSession) }
    }

    private fun transferData(session: WebSocketSession, jSchSession: Session) {
        val jSchInputStream = session.attributes["jSchInputStream"] as InputStream
        val buffer = ByteArray(1024)
        while (!Thread.currentThread().isInterrupted && jSchSession.isConnected) {
            val i = jSchInputStream.read(buffer)
            if (i == -1) break
            session.attributes["lastServerTimestamp"] = currentTimeMillis()
            val string = String(buffer, 0, i)
            session.sendMessage(
                TextMessage(objectMapper.writeValueAsString(Event(type = DATA, payload = string)))
            )
        }
        session.close(CloseStatus.NORMAL)
    }

    private fun dataTransmissionWatchdog(session: WebSocketSession, jSchSession: Session) {
        while (!Thread.currentThread().isInterrupted && jSchSession.isConnected) {
            val establishedTimestamp = session.attributes["establishedTimestamp"] as Long
            val lastServerTimestamp = session.attributes["lastServerTimestamp"] as Long?
            val lastClientTimestamp = session.attributes["lastClientTimestamp"] as Long?
            val currentTimestamp = currentTimeMillis()
            if (currentTimestamp - establishedTimestamp > timeout
                && currentTimestamp - (lastServerTimestamp ?: 0) > timeout
                && currentTimestamp - (lastClientTimestamp ?: 0) > timeout
            ) {
                logger.warn("[{}] No data transmission for a long time", session.id)
                session.close(CloseStatus.NORMAL)
            } else {
                Thread.sleep(1000)
            }
        }
    }
}
