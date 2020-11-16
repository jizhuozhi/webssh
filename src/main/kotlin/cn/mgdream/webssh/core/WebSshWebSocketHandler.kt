package cn.mgdream.webssh.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.jcraft.jsch.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.lang.System.currentTimeMillis

@Component
class WebSshWebSocketHandler(val objectMapper: ObjectMapper) : TextWebSocketHandler() {

    companion object {
        private val logger = LoggerFactory.getLogger(WebSshWebSocketHandler::class.java)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("[{}] Connection established!", session.id)
        session.attributes["establishedTimestamp"] = currentTimeMillis()
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        session.attributes["lastClientTimestamp"] = currentTimeMillis()
        val event = objectMapper.readValue<Event>(message.payload)
        val webSshEventHandler = WebSshEventHandler(objectMapper)
        webSshEventHandler.handleEvent(event, session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        logger.info("[{}] Connection closed! status: {}", session.id, status)
        val jSchSession = session.attributes["jSchSession"]
        if (jSchSession is Session) {
            jSchSession.disconnect()
        }
    }
}
