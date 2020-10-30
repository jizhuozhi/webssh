package cn.mgdream.webssh.core

import com.jcraft.jsch.JSch
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class WebSshHandshakeInterceptor : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: Map<String, Any>
    ): Boolean {
        val enabled = System.getenv("webssh.enabled")?.toBoolean() ?: false
        if (enabled) {
            val host: String? = System.getenv("webssh.host")
            val port: String? = System.getenv("webssh.port")
            val username: String? = System.getenv("webssh.username")
            val password: String? = System.getenv("webssh.password")
            val jSch = JSch()
            val jSchSession = jSch.getSession(username ?: "root", host ?: "localhost", port?.toInt() ?: 22)
            jSchSession.setPassword(password)
            if (attributes is MutableMap<String, Any>) {
                attributes["jSchSession"] = jSchSession
            }
        }
        return enabled
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
    }
}
