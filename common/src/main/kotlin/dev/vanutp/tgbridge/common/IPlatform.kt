package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import net.kyori.adventure.text.Component
import java.nio.file.Path

interface IPlatform {
    val name: String
    val configDir: Path
    val dataDir: Path

    fun broadcastMessage(recipients: List<ITgbridgePlayer>, text: Component)
    fun getOnlinePlayers(): List<ITgbridgePlayer>
    fun getLanguageKey(key: String): String?
    fun isModLoaded(modId: String): Boolean

    /**
     * Converts a native player object to a TgbridgePlayer.
     */
    fun playerToTgbridge(player: Any): ITgbridgePlayer?

    fun isModLoadedMulti(fabricId: String?, forgeId: String?, paperId: String?): Boolean {
        return when (this.name) {
            "fabric" -> fabricId != null && isModLoaded(fabricId)
            "forge" -> forgeId != null && isModLoaded(forgeId)
            "paper" -> paperId != null && isModLoaded(paperId)
            else -> false
        }
    }
}
