package cn.mgdream.webssh

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebSshApplication

fun main(args: Array<String>) {
    runApplication<WebSshApplication>(*args)
}
