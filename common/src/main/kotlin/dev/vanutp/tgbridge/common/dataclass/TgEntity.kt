package dev.vanutp.tgbridge.common.dataclass

data class TgEntity(
    val offset: Int?,
    var length: Int?,
    val type: String?,
    var url: String? = null,
)
