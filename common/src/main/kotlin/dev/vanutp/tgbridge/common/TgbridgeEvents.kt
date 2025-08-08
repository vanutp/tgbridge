package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.models.*

class SimpleEventHandler<E> internal constructor() {
    private val listeners = mutableListOf<suspend (E) -> Unit>()

    fun addSyncListener(listener: (E) -> Unit) {
        listeners.add(listener)
    }

    fun addListener(listener: suspend (E) -> Unit) {
        listeners.add(listener)
    }

    suspend fun invoke(event: E) {
        for (listener in listeners) {
            listener(event)
        }
    }
}

class CancellableEventHandler<E> internal constructor() {
    private val listeners = mutableListOf<suspend (E) -> EventResult>()

    fun addSyncListener(listener: (E) -> EventResult) {
        listeners.add(listener)
    }

    fun addListener(listener: suspend (E) -> EventResult) {
        listeners.add(listener)
    }

    suspend fun invoke(event: E): EventResult {
        for (listener in listeners) {
            if (listener(event) == EventResult.STOP) {
                return EventResult.STOP
            }
        }
        return EventResult.CONTINUE
    }
}

object TgbridgeEvents {
    // TODO: move TG_CHAT_MESSAGE to bot api layer
    val TG_CHAT_MESSAGE = CancellableEventHandler<TgMessage>()
    val MC_CHAT_MESSAGE = CancellableEventHandler<TgbridgeMcChatMessageEvent>()
    val DEATH = CancellableEventHandler<TgbridgeDeathEvent>()
    val JOIN = CancellableEventHandler<TgbridgeJoinEvent>()
    val LEAVE = CancellableEventHandler<TgbridgeLeaveEvent>()
    val ADVANCEMENT = CancellableEventHandler<TgbridgeAdvancementEvent>()
    val CONFIG_RELOAD = SimpleEventHandler<Unit>()
}

enum class EventResult {
    CONTINUE,
    STOP,
}
