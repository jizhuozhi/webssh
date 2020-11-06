package cn.mgdream.webssh.core

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
        val event = objectMapper.readValue<Event>(message.payload)
        val webSshEventHandler = WebSshEventHandler()
        webSshEventHandler.handleEvent(event, session)
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
                Thread.sleep(1000)
            }
        }
    }
}
