package dev.vanutp.tgbridge.common.models

import dev.vanutp.tgbridge.common.converters.TelegramFormattedText
import java.time.Instant

enum class LastMessageType {
    TEXT,
    LEAVE,
}

data class LastMessage(
    val type: LastMessageType,
    val id: Int,
    var date: Instant,
    var text: TelegramFormattedText? = null,
    val leftPlayer: String? = null,
)
