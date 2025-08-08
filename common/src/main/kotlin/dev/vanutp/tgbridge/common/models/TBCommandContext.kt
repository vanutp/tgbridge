package dev.vanutp.tgbridge.common.models

data class TBCommandContext(
    val source: TgbridgePlayer?,
    val reply: (text: String) -> Unit
)
