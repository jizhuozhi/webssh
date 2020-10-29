package cn.mgdream.webssh.core

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
class WebSocketConfiguration(val webSshWebSocketHandler: WebSshWebSocketHandler) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(webSshWebSocketHandler, "/ws/webssh")
    }
}
