package dev.vanutp.tgbridge.common.models

data class TBCommandContext(
    val reply: (text: String) -> Unit
)
