package dev.vanutp.tgbridge.common.dataclass

import com.google.gson.annotations.SerializedName

data class TgMessageOrigin(
    @SerializedName("sender_user")
    val senderUser: TgUser? = null,
    @SerializedName("sender_user_name")
    val senderUserName: String? = null,
    @SerializedName("sender_chat")
    val senderChat: TgChat? = null,
    val chat: TgChat? = null,
)