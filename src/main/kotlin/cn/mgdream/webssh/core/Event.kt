package cn.mgdream.webssh.core

data class Event(
    val type: EventType,
    val timestamp: Long,
    val payload: Any?
)

enum class EventType {
    COMMAND,
    DATA,
    INTERCEPT,
    NULL,
    RESIZE
}
