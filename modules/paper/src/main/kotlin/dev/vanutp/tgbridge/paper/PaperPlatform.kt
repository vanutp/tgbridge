package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.IResourceContainer
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import net.kyori.adventure.text.Component
import net.minecraft.locale.Language
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.absolute

class PaperPlatform(private val plugin: JavaPlugin) : IPlatform {
    override val name = "paper"
    override val gameDir = plugin.server.worldContainer.toPath().absolute()
    override val configDir = plugin.dataFolder.toPath().absolute()

    override fun broadcastMessage(recipients: List<ITgbridgePlayer>, text: Component) {
        plugin.server.consoleSender.sendMessage(text)
        recipients.forEach { player ->
            player.toNative()?.sendMessage(text)
        }
    }

    override fun getOnlinePlayers(): List<ITgbridgePlayer> {
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
        plugin.server.pluginManager.isPluginEnabled(modId)

    override fun getModResources() = emptyList<IResourceContainer>()
    override fun getMinecraftVersion() = plugin.server.minecraftVersion
    override fun playerToTgbridge(player: Any) = (player as Player).toTgbridge()
}
