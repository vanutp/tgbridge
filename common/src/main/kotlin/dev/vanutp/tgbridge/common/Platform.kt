package dev.vanutp.tgbridge.common

import net.kyori.adventure.text.Component
import java.nio.file.Path

abstract class Platform {
    abstract val name: String
    abstract val configDir: Path

    abstract fun registerChatMessageListener(handler: (TBChatMessageEvent) -> Unit)
    abstract fun broadcastMessage(text: Component)
}
