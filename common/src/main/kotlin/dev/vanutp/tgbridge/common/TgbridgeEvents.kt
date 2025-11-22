package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.models.*

enum class EventPriority {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST,
}

class TgbridgeEventHandler<E> internal constructor() {
    private val listeners = mutableMapOf<EventPriority, MutableList<suspend (E) -> Unit>>().apply {
        EventPriority.entries.forEach {
            this[it] = mutableListOf()
        }
    }

    fun addListener(priority: EventPriority, listener: Function1<E>) {
        listeners[priority]!!.add(listener::apply)
    }

    fun addListener(priority: EventPriority, listener: suspend (E) -> Unit) {
        listeners[priority]!!.add(listener)
    }

    fun addListener(listener: Function1<E>) {
        addListener(EventPriority.NORMAL, listener)
    }

    fun addListener(listener: suspend (E) -> Unit) {
        addListener(EventPriority.NORMAL, listener)
    }

    fun removeListener(priority: EventPriority, listener: Function1<E>) {
        listeners[priority]!!.remove(listener::apply)
    }

    fun removeListener(priority: EventPriority, listener: suspend (E) -> Unit) {
        listeners[priority]!!.remove(listener)
    }

    fun removeListener(listener: Function1<E>) {
        EventPriority.entries.forEach {
            listeners[it]!!.remove(listener::apply)
        }
    }

    fun removeListener(listener: suspend (E) -> Unit) {
        EventPriority.entries.forEach {
            listeners[it]!!.remove(listener)
        }
    }

    suspend fun invoke(event: E): Boolean {
        for (priority in EventPriority.entries) {
            for (listener in listeners[priority]!!) {
                listener(event)
                if (event is Cancellable && event.isCancelled) {
                    return false
                }
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
    val PLAYER_PLACEHOLDERS = TgbridgeEventHandler<TgbridgePlayerPlaceholdersEvent>()
    val RECIPIENTS = TgbridgeEventHandler<TgbridgeRecipientsEvent>()
}
