package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import net.kyori.adventure.text.Component
import net.minecraft.locale.Language
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.absolute

class PaperPlatform(private val plugin: JavaPlugin) : IPlatform {
    override val name = "paper"
    override val configDir = plugin.dataFolder.toPath().absolute()

    override fun broadcastMessage(text: Component) {
        plugin.server.consoleSender.sendMessage(text)
        for (p in plugin.server.onlinePlayers.filterNot { it.scoreboardTags.contains("hidden-telegram") }) {
            p.sendMessage(text)
        }
    }

    override fun getOnlinePlayers(): List<TgbridgePlayer> {
        return plugin.server.onlinePlayers.map { it.toTgbridge() }
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (has(key)) {
            this.getOrDefault(key)
        } else {
            null
        }
    }

    override fun isModLoaded(modId: String) =
        plugin.server.pluginManager.getPlugin(modId)?.isEnabled ?: false

    override fun playerToTgbridge(player: Any) = (player as Player).toTgbridge()
}
