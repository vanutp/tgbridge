package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.IPlatform
import dev.vanutp.tgbridge.common.MuteService
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import net.kyori.adventure.text.Component
import net.minecraft.locale.Language
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.absolute

class PaperPlatform(private val plugin: JavaPlugin) : IPlatform {
    override val name = "paper"
    override val configDir = plugin.dataFolder.toPath().absolute()

    private fun getRecipients(chat: ChatConfig): List<Player>? {
        val integration = TelegramBridge.INSTANCE.chatIntegration
        val players = if (integration == null) {
            plugin.server.onlinePlayers.takeIf { chat.isDefault }?.toList()
        } else {
            integration.getChatRecipients(chat, Player::class.java)
        }
        return players?.filterNot { MuteService.isMuted(it.uniqueId) }
    }

    override fun getChatRecipients(chat: ChatConfig) =
        getRecipients(chat)?.map { it.toTgbridge() }

    override fun broadcastMessage(chat: ChatConfig, text: Component) {
        plugin.server.consoleSender.sendMessage(text)
        getRecipients(chat)?.forEach { player ->
            player.sendMessage(text)
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
        plugin.server.pluginManager.isPluginEnabled(modId)

    override fun playerToTgbridge(player: Any) = (player as Player).toTgbridge()
}
