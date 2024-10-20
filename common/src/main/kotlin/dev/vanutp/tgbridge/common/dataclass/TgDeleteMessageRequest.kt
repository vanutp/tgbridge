package dev.vanutp.tgbridge.common.dataclass

import com.google.gson.annotations.SerializedName

data class TgDeleteMessageRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("message_id")
    val messageId: Int,
)
