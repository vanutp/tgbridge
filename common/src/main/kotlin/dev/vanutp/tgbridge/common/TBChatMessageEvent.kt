package dev.vanutp.tgbridge.common

import net.kyori.adventure.text.Component

data class TBChatMessageEvent(
    val username: String,
    val text: Component,
)
