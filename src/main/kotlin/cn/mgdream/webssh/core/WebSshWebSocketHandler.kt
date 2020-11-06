package cn.mgdream.webssh.core

import cn.mgdream.webssh.core.EventType.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jcraft.jsch.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.CloseStatus.NORMAL
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.OutputStream
import java.lang.System.currentTimeMillis
import java.util.concurrent.Executors
import kotlin.Int.Companion.MAX_VALUE

@Component
class WebSshWebSocketHandler(val objectMapper: ObjectMapper) : TextWebSocketHandler() {

    private val executorService = Executors.newFixedThreadPool(MAX_VALUE)

    companion object {
        private const val timeout = 60 * 1000L
        private val logger = LoggerFactory.getLogger(WebSshWebSocketHandler::class.java)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("[{}] Connection established!", session.id)
        session.attributes["establishedTimestamp"] = currentTimeMillis()
        val jSchSession = session.attributes["jSchSession"] as Session
        executorService.submit { transferData(session, jSchSession) }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        session.attributes["lastClientTimestamp"] = currentTimeMillis()
        val jSchOutputStream = session.attributes["jSchOutputStream"] as OutputStream
        val event = objectMapper.readValue<Event>(message.payload)
        val webSshEventHandler = WebSshEventHandler(session, jSchOutputStream)
        when (event.type) {
            COMMAND -> webSshEventHandler.handleCommandEvent(event)
            DATA -> webSshEventHandler.handleDataEvent(event)
            INTERCEPT -> webSshEventHandler.handleInterceptEvent(event)
            NULL -> webSshEventHandler.handleNullEvent(event)
            RESIZE -> webSshEventHandler.handleResizeEvent(event)
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.info("[{}] Connection closed! status: {}", session.id, status)
        val jSchSession = session.attributes["jSchSession"]
        if (jSchSession is Session) {
            jSchSession.disconnect()
        }
    }

    fun transferData(session: WebSocketSession, jSchSession: Session) {
        jSchSession.connect()
        val jSchChannel = jSchSession.openChannel("shell")
        jSchChannel.connect()
        val jSchInputStream = jSchChannel.inputStream
        val jSchOutputStream = jSchChannel.outputStream
        session.attributes["jSchSession"] = jSchSession
        session.attributes["jSchChannel"] = jSchChannel
        session.attributes["jSchInputStream"] = jSchInputStream
        session.attributes["jSchOutputStream"] = jSchOutputStream
        // Start watchdog after successful connection
        executorService.submit { dataTransmissionWatchdog(session, jSchSession) }
        val buffer = ByteArray(1024)
        while (!Thread.currentThread().isInterrupted && jSchSession.isConnected) {
            val i = jSchInputStream.read(buffer)
            if (i == -1) break
            session.attributes["lastServerTimestamp"] = currentTimeMillis()
            val string = String(buffer, 0, i)
            session.sendMessage(TextMessage(string))
        }
        session.close(NORMAL)
    }

    fun dataTransmissionWatchdog(session: WebSocketSession, jSchSession: Session) {
        var times = 0
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
                session.close(NORMAL)
            } else {
                val maxTimestamp = maxOf(establishedTimestamp, lastClientTimestamp ?: 0, lastServerTimestamp ?: 0)
                val offset = currentTimestamp - maxTimestamp
                if (offset > (times + 1) * 10000) {
                    times += 1
                    logger.warn("[{}] No data transmission more than {} seconds", session.id, times * 10)
                }
                Thread.sleep(1000)
            }
        }
    }
}
