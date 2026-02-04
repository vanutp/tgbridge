package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.models.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Function

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

    @Deprecated("This signature is deprecated, use addListener(EventPriority, Consumer<E>) instead", level = DeprecationLevel.WARNING)
    fun addListener(priority: EventPriority, listener: Function1<E>) {
        listeners[priority]!!.add(listener::apply)
    }

    fun addListener(priority: EventPriority, listener: Consumer<E>) {
        listeners[priority]!!.add(listener::accept)
    }

    fun addListener(priority: EventPriority, listener: Function<E, CompletableFuture<Void>>) {
        listeners[priority]!!.add {
            listener.apply(it).await()
        }
    }

    fun addListener(priority: EventPriority, listener: suspend (E) -> Unit) {
        listeners[priority]!!.add(listener)
    }

    @Deprecated("This signature is deprecated, use addListener(Consumer<E>) instead", level = DeprecationLevel.WARNING)
    fun addListener(listener: Function1<E>) {
        addListener(EventPriority.NORMAL, listener::apply)
    }

    fun addListener(listener: Consumer<E>) {
        addListener(EventPriority.NORMAL, listener)
    }

    fun addListener(listener: Function<E, CompletableFuture<Void>>) {
        addListener(EventPriority.NORMAL, listener)
    }

    fun addListener(listener: suspend (E) -> Unit) {
        addListener(EventPriority.NORMAL, listener)
    }

    @Deprecated("This signature is deprecated, use removeListener(handler) instead", level = DeprecationLevel.ERROR)
    fun removeListener(priority: EventPriority, listener: Function1<E>) {
        listeners[priority]!!.remove(listener::apply)
    }

    @Deprecated("This signature is deprecated, use removeListener(handler) instead", level = DeprecationLevel.ERROR)
    fun removeListener(priority: EventPriority, listener: Consumer<E>) {
        listeners[priority]!!.remove(listener::accept)
    }

    @Deprecated("This signature is deprecated, use removeListener(handler) instead", level = DeprecationLevel.ERROR)
    fun removeListener(priority: EventPriority, listener: Function<E, CompletableFuture<Void>>) {
        listeners[priority]!!.remove(listener::apply)
    }

    @Deprecated("This signature is deprecated, use removeListener(handler) instead", level = DeprecationLevel.ERROR)
    fun removeListener(priority: EventPriority, listener: suspend (E) -> Unit) {
        listeners[priority]!!.remove(listener)
    }

    @Deprecated("This signature is deprecated, use removeListener(Consumer<E>) instead", level = DeprecationLevel.WARNING)
    fun removeListener(listener: Function1<E>) {
        EventPriority.entries.forEach {
            listeners[it]!!.remove(listener::apply)
        }
    }

    fun removeListener(listener: Consumer<E>) {
        EventPriority.entries.forEach {
            listeners[it]!!.remove(listener::accept)
        }
    }

    fun removeListener(listener: Function<E, CompletableFuture<Void>>) {
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

    fun invokeAsync(event: E) = TelegramBridge.INSTANCE.coroutineScope.future {
        invoke(event)
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
