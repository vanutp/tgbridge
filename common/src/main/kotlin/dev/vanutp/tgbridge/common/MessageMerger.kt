package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.converters.TelegramFormattedText
import kotlinx.coroutines.sync.Mutex
import java.time.Instant
import java.util.UUID

enum class LastMessageType {
    TEXT,
    LEAVE,
}

data class LastMessage(
    val type: LastMessageType,
    val id: Int,
    var date: Instant,
    var text: TelegramFormattedText? = null,
    val leftPlayer: UUID? = null,
)


class MessageMerger {
    // TODO: automate this better
    val lastMessages = mutableMapOf<String, LastMessage>()
    // TODO: do we even need this? maybe a queue would be better?
    internal val lock = Mutex()

    internal fun unlock() {
        // TODO: this doesn't seem safe
        if (lock.isLocked) {
            lock.unlock()
        }
    }
}
