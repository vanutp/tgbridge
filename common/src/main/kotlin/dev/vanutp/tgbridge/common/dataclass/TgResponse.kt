package dev.vanutp.tgbridge.common.dataclass

data class TgResponse<T>(
    val ok: Boolean,
    val result: T?,
    val description: String?,
)
