package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import net.kyori.adventure.text.Component
import java.nio.file.Path

abstract class Platform {
    abstract val name: String
    abstract val configDir: Path

    abstract fun broadcastMessage(text: Component)
    abstract fun getOnlinePlayerNames(): Array<String>
    abstract fun getLanguageKey(key: String): String?
}
