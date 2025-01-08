package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class EventManager(private val plugin: PaperBootstrap) : Listener {
    fun register() {
        registerChatMessageListener()
        registerCommandHandlers()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    private fun registerPaperChatListener() {
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler(priority = EventPriority.LOWEST)
            fun onMessage(e: AsyncChatEvent) {
                plugin.tgbridge.onChatMessage(TBPlayerEventData(getPlayerName(e.player), e.message()))
            }
        }, plugin)
    }

    private fun registerLegacyChatListener() {
        plugin.logger.info("Using legacy chat event")
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler(priority = EventPriority.LOWEST)
            fun onMessage(e: AsyncPlayerChatEvent) {
                plugin.tgbridge.onChatMessage(TBPlayerEventData(getPlayerName(e.player), Component.text(e.message)))
            }
        }, plugin)
    }

    private fun shouldUseLegacyChatListener(): Boolean {
        return PaperConfigManager.config.compat.useLegacyChatListener
            ?: listOf("Chatty", "CMI").any { Bukkit.getPluginManager().isPluginEnabled(it) }
    }

    private fun registerChatMessageListener() {
        if (shouldUseLegacyChatListener()) {
            registerLegacyChatListener()
        } else {
            registerPaperChatListener()
        }
    }

    @EventHandler
    fun onPlayerAdvancement(e: PlayerAdvancementDoneEvent) {
        if (e.player.isVanished()) {
            return
        }
        plugin.tgbridge.onPlayerAdvancement(TBPlayerEventData(getPlayerName(e.player), e.message() ?: return))
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        if (e.player.isVanished()) {
            return
        }
        val username = getPlayerName(e.entity)
        val msg = e.deathMessage() ?: Component.translatable("death.attack.generic", Component.text(username))
        plugin.tgbridge.onPlayerDeath(TBPlayerEventData(username, msg))
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        if (e.player.isVanished()) {
            return
        }
        plugin.tgbridge.onPlayerJoin(getPlayerName(e.player))
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        if (e.player.isVanished()) {
            return
        }
        plugin.tgbridge.onPlayerLeave(getPlayerName(e.player))
    }

    private fun registerCommandHandlers() {
        plugin.getCommand("tgbridge")!!.setExecutor { commandSender, _, _, args ->
            if (args.toList() != listOf("reload")) {
                return@setExecutor false
            }
            return@setExecutor plugin.tgbridge.onReloadCommand(
                TBCommandContext(
                    reply = { text ->
                        commandSender.sendMessage(text)
                    }
                )
            )
        }
    }
}
