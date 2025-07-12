package dev.vanutp.tgbridge.common.models

import net.kyori.adventure.text.Component

interface TgbridgeEvent {
    val originalEvent: Any?
}

data class TgbridgeMcChatMessageEvent(
    var sender: TgbridgePlayer,
    var message: Component,
    override val originalEvent: Any?,
) : TgbridgeEvent

data class TgbridgeDeathEvent(
    var player: TgbridgePlayer,
    var message: Component?,
    override val originalEvent: Any?,
) : TgbridgeEvent

data class TgbridgeJoinEvent(
    var player: TgbridgePlayer,
    var hasPlayedBefore: Boolean,
    override val originalEvent: Any?,
) : TgbridgeEvent

data class TgbridgeLeaveEvent(
    var player: TgbridgePlayer,
    override val originalEvent: Any?,
) : TgbridgeEvent

data class TgbridgeAdvancementEvent(
    var player: TgbridgePlayer,
    var type: String,
    var title: Component,
    var description: Component,
    override val originalEvent: Any?,
) : TgbridgeEvent
