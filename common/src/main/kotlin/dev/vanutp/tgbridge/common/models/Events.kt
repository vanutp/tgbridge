package dev.vanutp.tgbridge.common.models

import dev.vanutp.tgbridge.common.Placeholders
import dev.vanutp.tgbridge.common.TgMessage
import net.kyori.adventure.text.Component

interface SyntheticEvent {
    val originalEvent: Any?
}

interface Cancellable {
    var isCancelled: Boolean
}

data class TgbridgeTgChatMessageEvent(
    var chat: ChatConfig,
    var message: TgMessage,
    var placeholders: Placeholders = Placeholders(),
    override var isCancelled: Boolean = false,
) : Cancellable

data class TgbridgeMcChatMessageEvent(
    var sender: ITgbridgePlayer,
    var message: Component,
    var chatName: String?,
    override val originalEvent: Any?,
    override var isCancelled: Boolean = false,
) : SyntheticEvent, Cancellable

data class TgbridgeDeathEvent(
    var player: ITgbridgePlayer,
    var message: Component?,
    override val originalEvent: Any?,
    override var isCancelled: Boolean = false,
) : SyntheticEvent, Cancellable

data class TgbridgeJoinEvent(
    var player: ITgbridgePlayer,
    var hasPlayedBefore: Boolean,
    override val originalEvent: Any?,
    var ignoreVanish: Boolean = false,
    override var isCancelled: Boolean = false,
) : SyntheticEvent, Cancellable

data class TgbridgeLeaveEvent(
    var player: ITgbridgePlayer,
    override val originalEvent: Any?,
    var ignoreVanish: Boolean = false,
    override var isCancelled: Boolean = false,
) : SyntheticEvent, Cancellable

data class TgbridgeAdvancementEvent(
    var player: ITgbridgePlayer,
    var type: String,
    var title: Component,
    var description: Component,
    override val originalEvent: Any?,
    override var isCancelled: Boolean = false,
) : SyntheticEvent, Cancellable

data class TgbridgePlayerPlaceholdersEvent(
    var player: ITgbridgePlayer,
    var placeholders: Placeholders = Placeholders(),
    val originalEvent: Any?,
)

data class TgbridgeRecipientsEvent(
    var chat: ChatConfig,
    var recipients: List<ITgbridgePlayer> = emptyList(),
    val originalEvent: Any?,
)
