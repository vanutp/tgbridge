package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.Platform
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import dev.vanutp.tgbridge.common.translate
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.absolute
import net.minecraft.locale.Language

class PaperPlatform(private val plugin: JavaPlugin) : Platform() {
    override val name = "paper"
    override val configDir = plugin.dataFolder.toPath().absolute()

    override fun registerChatMessageListener(handler: (TBPlayerEventData) -> Unit) {
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onMessage(e: AsyncChatEvent) {
                val username = (e.player.displayName() as TextComponent).translate()
                handler.invoke(TBPlayerEventData(username, e.message()))
            }
        }, plugin)
    }

    override fun registerPlayerAdvancementListener(handler: (TBPlayerEventData) -> Unit) {
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onMessage(e: PlayerAdvancementDoneEvent) {
                val username = (e.player.displayName() as TextComponent).translate()
                val msg = e.message() ?: return
                handler.invoke(TBPlayerEventData(username, msg))
            }
        }, plugin)
    }

    override fun registerPlayerDeathListener(handler: (TBPlayerEventData) -> Unit) {
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onMessage(e: PlayerDeathEvent) {
                val username = (e.player.displayName() as TextComponent).translate()
                val msg = e.deathMessage() ?: Component.translatable("death.attack.generic", Component.text(username))
                handler.invoke(TBPlayerEventData(username, msg))
            }
        }, plugin)
    }

    override fun registerPlayerJoinListener(handler: (TBPlayerEventData) -> Unit) {
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onMessage(e: PlayerJoinEvent) {
                val username = (e.player.displayName() as TextComponent).translate()
                handler.invoke(TBPlayerEventData(username, e.joinMessage() ?: Component.text("")))
            }
        }, plugin)
    }

    override fun registerPlayerLeaveListener(handler: (TBPlayerEventData) -> Unit) {
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onMessage(e: PlayerQuitEvent) {
                val username = (e.player.displayName() as TextComponent).translate()
                handler.invoke(TBPlayerEventData(username, e.quitMessage() ?: Component.text("")))
            }
        }, plugin)
    }

    override fun registerCommand(command: Array<String>, handler: (TBCommandContext) -> Boolean) {
        plugin.getCommand(command[0])!!.setExecutor { commandSender, _, _, args ->
            command.drop(1).forEachIndexed { i, x ->
                if (x != args[i]) {
                    return@setExecutor false
                }
            }
            return@setExecutor handler(
                TBCommandContext(
                    reply = { text ->
                        commandSender.sendMessage(text)
                    }
                )
            )
        }
    }

    override fun broadcastMessage(text: Component) {
        plugin.server.broadcast(text)
    }

    override fun getOnlinePlayerNames(): Array<String> {
        return plugin.server.onlinePlayers.map { it.name }.toTypedArray()
    }

    override fun getLanguageKey(key: String) = with(Language.getInstance()) {
        if (has(key)) {
            this.getOrDefault(key)
        } else {
            null
        }
    }
}
