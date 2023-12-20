package dev.vanutp.tgbridge.common.models

import net.kyori.adventure.text.Component

data class TBPlayerEventData(
    val username: String,
    val text: Component,
)
