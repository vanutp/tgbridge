package dev.vanutp.tgbridge.common.models

import java.time.Instant

enum class LastMessageType {
    TEXT,
    LEAVE,
}

data class LastMessage(
    val type: LastMessageType,
    val id: Int,
    var date: Instant,
    var text: String? = null,
    val leftPlayer: String? = null,
)
