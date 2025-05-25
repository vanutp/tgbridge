package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.Platform
import net.kyori.adventure.text.Component
import net.minecraft.locale.Language
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.absolute

class PaperPlatform(private val plugin: JavaPlugin) : Platform() {
    override val name = "paper"
    override val configDir = plugin.dataFolder.toPath().absolute()

    override fun broadcastMessage(text: Component) {
        plugin.server.consoleSender.sendMessage(text)
        for (p in plugin.server.onlinePlayers.filterNot { it.scoreboardTags.contains("hidden-telegram") }) {
            p.sendMessage(text)
        }
    }

    override fun getOnlinePlayerNames(): Array<String> {
        return plugin.server.onlinePlayers
            .filterNot { it.isVanished() }
            .map { it.name }
            .toTypedArray()
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (has(key)) {
            this.getOrDefault(key)
        } else {
            null
        }
    }
}
