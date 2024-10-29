package dev.vanutp.tgbridge.common.models

import dev.vanutp.tgbridge.common.dataclass.TgEntity
import net.kyori.adventure.text.Component
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
    var entities: List<TgEntity>? = null,
    val leftPlayer: String? = null,
    var componentOfLastAppend: Component? = null
)
