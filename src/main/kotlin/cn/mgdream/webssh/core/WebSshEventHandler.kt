package cn.mgdream.webssh.core

import com.jcraft.jsch.ChannelShell
import org.springframework.web.socket.WebSocketSession
import java.io.OutputStream

class WebSshEventHandler(
    val session: WebSocketSession,
    val outputStream: OutputStream
) : EventHandler {

    override fun handleCommandEvent(event: Event) {
        val payload = event.payload as String
        outputStream.write(payload.toByteArray(Charsets.UTF_8))
        outputStream.write('\r'.toInt())
        outputStream.flush()
    }

    override fun handleDataEvent(event: Event) {
        val payload = event.payload as String
        outputStream.write(payload.toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    override fun handleInterceptEvent(event: Event) {
        outputStream.write('\u0003'.toInt())
        outputStream.flush()
    }

    override fun handleNullEvent(event: Event) {
        outputStream.write('\r'.toInt())
        outputStream.flush()
    }

    override fun handleResizeEvent(event: Event) {
        val jSchChannel = session.attributes["jSchChannel"] as ChannelShell
        val payload = event.payload as Map<*, *>
        val cols = payload["cols"].toString().toInt()
        val rows = payload["rows"].toString().toInt()
        jSchChannel.setPtySize(cols, rows, cols * 8, rows * 8)

    }

}
