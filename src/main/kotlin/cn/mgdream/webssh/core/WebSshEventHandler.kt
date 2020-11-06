package cn.mgdream.webssh.core

import com.jcraft.jsch.ChannelShell
import org.springframework.web.socket.WebSocketSession
import java.io.OutputStream

open class WebSshEventHandler {

    fun handleEvent(event: Event, session: WebSocketSession) {
        when (event.type) {
            EventType.COMMAND -> handleCommandEvent(event, session)
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

}
