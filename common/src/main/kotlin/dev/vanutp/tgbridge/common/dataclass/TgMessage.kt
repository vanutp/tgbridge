package dev.vanutp.tgbridge.common.dataclass

import com.google.gson.annotations.SerializedName
import dev.vanutp.tgbridge.common.models.TgMessageMedia

data class TgMessage(
    val chat: TgChat,
    @SerializedName("message_id")
    val messageId: Int,
    val from: TgUser? = null,
    @SerializedName("sender_chat")
    val senderChat: TgChat? = null,
    @SerializedName("forward_from")
    val forwardFrom: TgUser? = null,
    @SerializedName("forward_from_chat")
    val forwardFromChat: TgChat? = null,
    @SerializedName("reply_to_message")
    val replyToMessage: TgMessage? = null,
    @SerializedName("external_reply")
    val externalReply: TgExternalReplyInfo? = null,
    val quote: TgTextQuote? = null,
    @SerializedName("message_thread_id")
    val messageThreadId: Int? = null,
    @SerializedName("author_signature")
    val authorSignature: String? = null,
    val text: String? = null,
    val caption: String? = null,
    override val animation: TgAny? = null,
    override val photo: List<TgAny>? = null,
    override val audio: TgAny? = null,
    override val document: TgAny? = null,
    override val sticker: TgAny? = null,
    override val video: TgAny? = null,
    @SerializedName("video_note")
    override val videoNote: TgAny? = null,
    override val voice: TgAny? = null,
    override val poll: TgPoll? = null,
    @SerializedName("pinned_message")
    val pinnedMessage: TgMessage? = null,
) : TgMessageMedia {
    val senderName
        get() = authorSignature
            ?: senderChat?.title
            ?: from?.let {
                (it.firstName + " " + (it.lastName ?: "")).trim()
            }
            ?: ""
    val senderUserName
        get() = from?.username
            ?: senderChat?.username
            ?: ""
    val effectiveText
        get() = text ?: caption
}
