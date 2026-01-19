package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.ConfigManager
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.models.*
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerLoadEvent
import java.util.*

class EventManager(private val plugin: PaperBootstrap) : Listener {
    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (!ConfigManager.config.auth.enabled) {
            return
        }
        val playerName = event.name
        val telegramId = plugin.tgbridge.authStorage.getTelegramId(playerName)
        if (telegramId == null) {
            val code = (10000..999999).random().toString()
            plugin.tgbridge.authCodes[playerName.lowercase()] = code
            val kickMessage = ConfigManager.lang.auth.kickMessage.replace("{code}", code)
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MiniMessage.miniMessage().deserialize(kickMessage))
        }
    }
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
                if (e.isCancelled || TelegramBridge.INSTANCE.chatModule != null) {
                    return
                }
                plugin.tgbridge.onChatMessage(
                    TgbridgeMcChatMessageEvent(
                        e.player.toTgbridge(),
                        e.message(),
                        null,
                        e,
                    )
                )
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
                if (ConfigManager.config.auth.enabled) {
                    val player = e.player
                    val telegramId = plugin.tgbridge.authStorage.getTelegramId(player.name)
                    if (telegramId != null) {
                        plugin.tgbridge.coroutineScope.launch {
                            val isMember = plugin.tgbridge.bot.isUserInGroup(telegramId, ConfigManager.config.auth.groupId)
                            if (!isMember) {
                                plugin.server.scheduler.runTask(plugin, Runnable {
                                    player.kick(MiniMessage.miniMessage().deserialize(ConfigManager.lang.auth.notInGroupMessage))
                                })
                            }
                        }
                    }
                }
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

    private fun onSendCommand(ctx: TBCommandContext, args: Array<String>): Boolean {
        val format = args[1]
        val chatName = args[2]
        val message = args.slice(3 until args.size).joinToString(" ")
        return plugin.tgbridge.onSendCommand(ctx, format, chatName, message)
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
            if (args.contentDeepEquals(arrayOf("unlink"))) {
                val player = commandSender.toTgbridge()
                val source = player.source
                if (source == null) {
                    return@setExecutor false
                }
                if (plugin.tgbridge.authStorage.getTelegramId(source.getName()) != null) {
                    plugin.tgbridge.authStorage.unlinkPlayer(source.getName())
                    player.reply(ConfigManager.lang.auth.unlinkSuccess)
                } else {
                    player.reply(ConfigManager.lang.auth.notLinked)
                }
                return@setExecutor true
            }
            if (args.size >= 4 && args[0] == "send") {
                if (!commandSender.isOp) {
                    return@setExecutor false
                }
                return@setExecutor onSendCommand(commandSender.toTgbridge(), args)
            }
            return@setExecutor false
        }
    }
}
