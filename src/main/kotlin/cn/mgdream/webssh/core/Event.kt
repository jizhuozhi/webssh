package cn.mgdream.webssh.core

import java.lang.System.currentTimeMillis

data class Event(
    val type: EventType,
    val timestamp: Long = currentTimeMillis(),
    val payload: Any? = null
)

enum class EventType {
    COMMAND,
    CONNECT,
    DATA,
    INTERCEPT,
    NULL,
    RESIZE
}
