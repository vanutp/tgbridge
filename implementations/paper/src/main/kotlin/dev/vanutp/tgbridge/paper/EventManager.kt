package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.models.*
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerLoadEvent

class EventManager(private val plugin: PaperBootstrap) : Listener {
    fun register() {
        registerChatMessageListener()
        registerJoinLeaveListener()
        registerCommandHandlers()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler()
    fun onServerLoad(e: ServerLoadEvent) {
        plugin.tgbridge.onServerStarted()
    }

    private fun registerChatMessageListener() {
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler(priority = EventPriority.MONITOR)
            fun onMessage(e: AsyncChatEvent) {
                if (e.isCancelled) {
                    return
                }
                plugin.tgbridge.onChatMessage(TgbridgeMcChatMessageEvent(e.player.toTgbridge(), e.message(), e))
            }
        }, plugin)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerAdvancement(e: PlayerAdvancementDoneEvent) {
        val display = e.advancement.display
        if (display == null || !display.doesAnnounceToChat()) {
            return
        }
        val type = display.frame().name.lowercase()
        plugin.tgbridge.onPlayerAdvancement(
            TgbridgeAdvancementEvent(
                e.player.toTgbridge(),
                type,
                display.title(),
                display.description(),
                e,
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val msg = e.deathMessage()
        plugin.tgbridge.onPlayerDeath(TgbridgeDeathEvent(e.player.toTgbridge(), msg, e))
    }

    private fun registerJoinLeaveListener() {
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler(priority = EventPriority.MONITOR)
            fun onPlayerJoin(e: PlayerJoinEvent) {
                plugin.tgbridge.onPlayerJoin(
                    TgbridgeJoinEvent(
                        e.player.toTgbridge(),
                        e.player.hasPlayedBefore(),
                        e,
                    )
                )
            }

            @EventHandler(priority = EventPriority.MONITOR)
            fun onPlayerQuit(e: PlayerQuitEvent) {
                plugin.tgbridge.onPlayerLeave(TgbridgeLeaveEvent(e.player.toTgbridge(), e))
            }
        }, plugin)
    }

    private fun registerCommandHandlers() {
        plugin.getCommand("tgbridge")!!.setExecutor { commandSender, _, _, args ->
            if (args.contentDeepEquals(arrayOf("reload"))) {
                if (!commandSender.isOp) {
                    return@setExecutor false
                }
                return@setExecutor plugin.tgbridge.onReloadCommand(commandSender.toTgbridge())
            }
            if (args.contentDeepEquals(arrayOf("toggle"))) {
                return@setExecutor plugin.tgbridge.onToggleMuteCommand(commandSender.toTgbridge())
            }
            return@setExecutor false
        }
    }
}
