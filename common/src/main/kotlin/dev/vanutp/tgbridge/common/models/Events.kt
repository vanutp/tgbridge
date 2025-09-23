package dev.vanutp.tgbridge.common.models

import net.kyori.adventure.text.Component

interface TgbridgeEvent {
    val originalEvent: Any?
    val metadata: Map<String, Any>
}

data class TgbridgeMcChatMessageEvent(
    var sender: TgbridgePlayer,
    var message: Component,
    override val originalEvent: Any?,
    override val metadata: Map<String, Any> = emptyMap(),
) : TgbridgeEvent

data class TgbridgeDeathEvent(
    var player: TgbridgePlayer,
    var message: Component?,
    override val originalEvent: Any?,
    override val metadata: Map<String, Any> = emptyMap(),
) : TgbridgeEvent

data class TgbridgeJoinEvent(
    var player: TgbridgePlayer,
    var hasPlayedBefore: Boolean,
    override val originalEvent: Any?,
    override val metadata: Map<String, Any> = emptyMap(),
    var ignoreVanish: Boolean = false,
) : TgbridgeEvent

data class TgbridgeLeaveEvent(
    var player: TgbridgePlayer,
    override val originalEvent: Any?,
    override val metadata: Map<String, Any> = emptyMap(),
    var ignoreVanish: Boolean = false,
) : TgbridgeEvent

data class TgbridgeAdvancementEvent(
    var player: TgbridgePlayer,
    var type: String,
    var title: Component,
    var description: Component,
    override val originalEvent: Any?,
    override val metadata: Map<String, Any> = emptyMap(),
) : TgbridgeEvent
