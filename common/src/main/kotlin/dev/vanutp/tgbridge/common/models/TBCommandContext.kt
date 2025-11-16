package dev.vanutp.tgbridge.common.models

data class TBCommandContext(
    val source: ITgbridgePlayer?,
    val reply: (text: String) -> Unit
)
