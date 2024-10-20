package dev.vanutp.tgbridge.common.dataclass

data class TgChat(
    val id: Long,
    val title: String = "",
    val username: String? = null,
)
