package dev.vanutp.tgbridge.common.models

import dev.vanutp.tgbridge.common.TgMessage
import net.kyori.adventure.text.Component

interface MinecraftEvent {
    val originalEvent: Any?
    val metadata: Map<String, Any>
}

interface Cancellable {
    var isCancelled: Boolean
}

data class TgbridgeTgChatMessageEvent(
    var message: TgMessage,
    override var isCancelled: Boolean = false,
) : Cancellable

data class TgbridgeMcChatMessageEvent(
    var sender: TgbridgePlayer,
    var message: Component,
    override val originalEvent: Any?,
    override val metadata: Map<String, Any> = emptyMap(),
    override var isCancelled: Boolean = false,
) : MinecraftEvent, Cancellable

data class TgbridgeDeathEvent(
    var player: TgbridgePlayer,
    var message: Component?,
    override val originalEvent: Any?,
    override val metadata: Map<String, Any> = emptyMap(),
    override var isCancelled: Boolean = false,
) : MinecraftEvent, Cancellable

data class TgbridgeJoinEvent(
    var player: TgbridgePlayer,
    var hasPlayedBefore: Boolean,
    override val originalEvent: Any?,
    override val metadata: Map<String, Any> = emptyMap(),
    var ignoreVanish: Boolean = false,
    override var isCancelled: Boolean = false,
) : MinecraftEvent, Cancellable

data class TgbridgeLeaveEvent(
    var player: TgbridgePlayer,
    override val originalEvent: Any?,
    override val metadata: Map<String, Any> = emptyMap(),
    var ignoreVanish: Boolean = false,
    override var isCancelled: Boolean = false,
) : MinecraftEvent, Cancellable

data class TgbridgeAdvancementEvent(
    var player: TgbridgePlayer,
    var type: String,
    var title: Component,
    var description: Component,
    override val originalEvent: Any?,
    override val metadata: Map<String, Any> = emptyMap(),
    override var isCancelled: Boolean = false,
) : MinecraftEvent, Cancellable
