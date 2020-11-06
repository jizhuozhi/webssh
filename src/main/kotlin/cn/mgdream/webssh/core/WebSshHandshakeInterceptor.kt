package cn.mgdream.webssh.core

import com.jcraft.jsch.JSch
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class WebSshHandshakeInterceptor(val webSshProperties: WebSshProperties) : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: Map<String, Any>
    ): Boolean {
        if (!webSshProperties.enabled) return false
        if (webSshProperties.auto) {
            val jSch = JSch()
            val jSchSession = jSch.getSession(
                webSshProperties.username,
                webSshProperties.hostname,
                webSshProperties.port ?: 22
            )
            jSchSession.setPassword(webSshProperties.password)
            jSchSession.userInfo = object : UserInfoAdapter() {
                override fun promptYesNo(message: String?): Boolean {
                    return true
                }
            }
            if (attributes is MutableMap<String, Any>) {
                attributes["jSchSession"] = jSchSession
            }
        }
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
    }
}
