package cn.mgdream.webssh.core

import cn.mgdream.webssh.core.EventType.*

interface EventHandler {

    fun handleEvent(event: Event) {
        when (event.type) {
            COMMAND -> handleCommandEvent(event)
            DATA -> handleDataEvent(event)
            INTERCEPT -> handleInterceptEvent(event)
            NULL -> handleNullEvent(event)
            RESIZE -> handleResizeEvent(event)
        }
    }

    fun handleCommandEvent(event: Event)

    fun handleDataEvent(event: Event)

    fun handleInterceptEvent(event: Event)

    fun handleNullEvent(event: Event)

    fun handleResizeEvent(event: Event)
}
