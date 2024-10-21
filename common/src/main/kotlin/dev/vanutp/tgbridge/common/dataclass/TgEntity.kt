package dev.vanutp.tgbridge.common.dataclass

data class TgEntity(
    val offset: Int?,
    val length: Int?,
    val type: String?,
    val url: String? = null,
)
