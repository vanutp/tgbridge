package dev.vanutp.tgbridge.common

import net.kyori.adventure.text.Component
import java.nio.file.Path

abstract class Platform {
    abstract val name: String
    abstract val configDir: Path

    abstract fun registerChatMessageListener(handler: (TBPlayerEventData) -> Unit)
    abstract fun registerPlayerAdvancementListener(handler: (TBPlayerEventData) -> Unit)
    abstract fun registerPlayerDeathListener(handler: (TBPlayerEventData) -> Unit)
    abstract fun registerPlayerJoinListener(handler: (TBPlayerEventData) -> Unit)
    abstract fun registerPlayerLeaveListener(handler: (TBPlayerEventData) -> Unit)
    abstract fun broadcastMessage(text: Component)
}
