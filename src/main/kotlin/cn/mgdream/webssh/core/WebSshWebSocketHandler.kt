package cn.mgdream.webssh.core

import com.jcraft.jsch.Session
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.OutputStream
import java.util.concurrent.Executors
import kotlin.Int.Companion.MAX_VALUE

@Component
class WebSshWebSocketHandler : TextWebSocketHandler() {

    private val executorService = Executors.newFixedThreadPool(MAX_VALUE)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val jSchSession = session.attributes["jSchSession"] as Session
        executorService.submit {
            session.sendMessage(TextMessage("Connecting..."))
            jSchSession.connect()
            session.sendMessage(TextMessage("Connect successfully!"))
            session.sendMessage(TextMessage("\u001b[H\u001b[2J"))
            val jSchChannel = jSchSession.openChannel("shell")
            jSchChannel.connect()
            val jSchInputStream = jSchChannel.inputStream
            val jSchOutputStream = jSchChannel.outputStream
            session.attributes["jSchSession"] = jSchSession
            session.attributes["jSchInputStream"] = jSchInputStream
            session.attributes["jSchOutputStream"] = jSchOutputStream
            val buffer = ByteArray(1024)
            while (!Thread.currentThread().isInterrupted && jSchSession.isConnected) {
                val i = jSchInputStream.read(buffer)
                if (i == -1) break
                val string = String(buffer, 0, i)
                session.sendMessage(TextMessage(string))
            }
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val jSchOutputStream = session.attributes["jSchOutputStream"]
        if (jSchOutputStream is OutputStream) {
            jSchOutputStream.write(message.payload.toByteArray())
            jSchOutputStream.flush()
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val jSchSession = session.attributes["jSchSession"]
        if (jSchSession is Session) {
            jSchSession.disconnect()
        }
    }
}
