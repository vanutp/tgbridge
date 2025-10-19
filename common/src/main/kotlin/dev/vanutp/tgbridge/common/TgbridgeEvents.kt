package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.models.*

fun interface EventListener<E> {
    fun onEvent(event: E)
}

class TgbridgeEventHandler<E> internal constructor() {
    private val listeners = mutableListOf<suspend (E) -> Unit>()

    fun addListener(listener: EventListener<E>) {
        listeners.add(listener::onEvent)
    }

    fun addListener(listener: suspend (E) -> Unit) {
        listeners.add(listener)
    }

    suspend fun invoke(event: E): Boolean {
        for (listener in listeners) {
            listener(event)
            if (event is Cancellable && event.isCancelled) {
                return false
            }
        }
        return true
    }
}

object TgbridgeEvents {
    // TODO: move TG_CHAT_MESSAGE to bot api layer
    val TG_CHAT_MESSAGE = TgbridgeEventHandler<TgbridgeTgChatMessageEvent>()
    val MC_CHAT_MESSAGE = TgbridgeEventHandler<TgbridgeMcChatMessageEvent>()
    val DEATH = TgbridgeEventHandler<TgbridgeDeathEvent>()
    val JOIN = TgbridgeEventHandler<TgbridgeJoinEvent>()
    val LEAVE = TgbridgeEventHandler<TgbridgeLeaveEvent>()
    val ADVANCEMENT = TgbridgeEventHandler<TgbridgeAdvancementEvent>()
    val CONFIG_RELOAD = TgbridgeEventHandler<Unit>()
}
