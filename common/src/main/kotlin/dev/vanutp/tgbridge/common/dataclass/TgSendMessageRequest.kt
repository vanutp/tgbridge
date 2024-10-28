package dev.vanutp.tgbridge.common.dataclass

import com.google.gson.annotations.SerializedName

data class TgSendMessageRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("text")
    val text: String,
    @SerializedName("reply_to_message_id")
    val replyToMessageId: Int? = null,
    @SerializedName("parse_mode")
    val parseMode: String = "MarkdownV2",
    @SerializedName("disable_web_page_preview")
    val disableWebPagePreview: Boolean = true,
    val entities: List<TgEntity>? = null,
)
