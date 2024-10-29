package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import net.kyori.adventure.text.Component
import java.nio.file.Path

abstract class Platform {
    abstract val name: String
    abstract val configDir: Path
    abstract val placeholderAPIInstance: PlaceholderAPI?
    abstract val styledChatInstance: StyledChat?

    abstract fun registerChatMessageListener(handler: (TBPlayerEventData) -> Unit)
    abstract fun registerPlayerAdvancementListener(handler: (TBPlayerEventData) -> Unit)
    abstract fun registerPlayerDeathListener(handler: (TBPlayerEventData) -> Unit)
    abstract fun registerPlayerJoinListener(handler: (TBPlayerEventData) -> Unit)
    abstract fun registerPlayerLeaveListener(handler: (TBPlayerEventData) -> Unit)
    abstract fun registerCommand(command: Array<String>, handler: (TBCommandContext) -> Boolean)
    abstract fun broadcastMessage(text: Component)
    abstract fun getOnlinePlayerNames(): Array<String>
    abstract fun getLanguageKey(key: String): String?
}
