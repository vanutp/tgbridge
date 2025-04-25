package dev.vanutp.tgbridge.common.models

import net.kyori.adventure.text.Component

data class TBPlayerEventData(
    val username: String,
    val text: Component,
)

data class TBAdvancementEvent(
    val username: String,
    val type: String,
    val title: Component,
    val description: Component,
)
