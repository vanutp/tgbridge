package dev.vanutp.tgbridge.common.models

import dev.vanutp.tgbridge.common.Placeholders
import dev.vanutp.tgbridge.common.TgMessage
import net.kyori.adventure.text.Component

interface MinecraftEvent {
    val originalEvent: Any?
}

interface MessageProducingEvent {
    var placeholders: Placeholders
}

interface Cancellable {
    var isCancelled: Boolean
}

data class TgbridgeTgChatMessageEvent(
    var message: TgMessage,
    var chat: ChatConfig,
    override var isCancelled: Boolean = false,
    override var placeholders: Placeholders = Placeholders(),
) : Cancellable, MessageProducingEvent

data class TgbridgeMcChatMessageEvent(
    var sender: ITgbridgePlayer,
    var message: Component,
    var chatName: String?,
    override val originalEvent: Any?,
    override var isCancelled: Boolean = false,
    override var placeholders: Placeholders = Placeholders(),
) : MinecraftEvent, Cancellable, MessageProducingEvent

data class TgbridgeDeathEvent(
    var player: ITgbridgePlayer,
    var message: Component?,
    override val originalEvent: Any?,
    override var isCancelled: Boolean = false,
    override var placeholders: Placeholders = Placeholders(),
) : MinecraftEvent, Cancellable, MessageProducingEvent

data class TgbridgeJoinEvent(
    var player: ITgbridgePlayer,
    var hasPlayedBefore: Boolean,
    override val originalEvent: Any?,
    var ignoreVanish: Boolean = false,
    override var isCancelled: Boolean = false,
    override var placeholders: Placeholders = Placeholders(),
) : MinecraftEvent, Cancellable, MessageProducingEvent

data class TgbridgeLeaveEvent(
    var player: ITgbridgePlayer,
    override val originalEvent: Any?,
    var ignoreVanish: Boolean = false,
    override var isCancelled: Boolean = false,
    override var placeholders: Placeholders = Placeholders(),
) : MinecraftEvent, Cancellable, MessageProducingEvent

data class TgbridgeAdvancementEvent(
    var player: ITgbridgePlayer,
    var type: String,
    var title: Component,
    var description: Component,
    override val originalEvent: Any?,
    override var isCancelled: Boolean = false,
    override var placeholders: Placeholders = Placeholders(),
) : MinecraftEvent, Cancellable, MessageProducingEvent
