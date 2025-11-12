package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import net.kyori.adventure.text.Component
import java.nio.file.Path
import java.util.UUID

interface IPlatform {
    val name: String
    val configDir: Path

    fun broadcastMessage(chat: ChatConfig, text: Component)
    fun getChatRecipients(chat: ChatConfig): List<TgbridgePlayer>?
    fun getOnlinePlayers(): List<TgbridgePlayer>
    fun getLanguageKey(key: String): String?
    fun isModLoaded(modId: String): Boolean

    /**
     * Converts a native player object to a TgbridgePlayer.
     */
    fun playerToTgbridge(player: Any): TgbridgePlayer?

    fun isModLoadedMulti(fabricId: String?, forgeId: String?, paperId: String?): Boolean {
        return when (this.name) {
            "fabric" -> fabricId != null && isModLoaded(fabricId)
            "forge" -> forgeId != null && isModLoaded(forgeId)
            "paper" -> paperId != null && isModLoaded(paperId)
            else -> false
        }
    }
}
