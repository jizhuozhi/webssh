package cn.mgdream.webssh.core

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("webssh")
@Component
class WebSshProperties {
    var auto: Boolean = false
    var hostname: String? = "localhost"
    var port: Int? = 22
    var username: String? = "root"
    var password: String? = null
    var enabled: Boolean = false

    override fun toString(): String {
        return "WebSshProperties(auto=${auto}, hostname=$hostname, port=$port, username=$username, password=[])"
    }
}
