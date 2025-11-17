package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.models.*

class TgbridgeEventHandler<E> internal constructor() {
    private val listeners = mutableListOf<suspend (E) -> Unit>()

    fun addListener(listener: Function1<E>) {
        listeners.add(listener::apply)
    }

    fun addListener(listener: suspend (E) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: Function1<E>) {
        listeners.remove(listener::apply)
    }

    fun removeListener(listener: suspend (E) -> Unit) {
        listeners.remove(listener)
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
    val POST_RELOAD = TgbridgeEventHandler<Unit>()
}
