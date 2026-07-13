package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.TelegramBridge
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
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import kotlinx.coroutines.runBlocking
import dev.vanutp.tgbridge.common.AuthManager
import dev.vanutp.tgbridge.common.ConfigManager
import dev.vanutp.tgbridge.common.TgInlineKeyboardButton
import dev.vanutp.tgbridge.common.TgInlineKeyboardMarkup

class EventManager(private val plugin: PaperBootstrap) : Listener {
    fun register() {
        registerChatMessageListener()
        registerJoinLeaveListener()
        registerCommandHandlers()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerPreLogin(e: AsyncPlayerPreLoginEvent) {
        val username = e.name
        val ip = e.address.hostAddress

        val bridge = TelegramBridge.INSTANCE
        val config = ConfigManager.config

        // 1. Get or create player in database
        val player = AuthManager.getOrCreatePlayer(username)

        // 2. Check if linked (has tgId)
        if (player.tgId == null) {
            val code = player.currentCode ?: AuthManager.generateCode(player)
            // Save the connecting IP as a pending IP, so that once they link, we can auto-authorize this IP!
            player.pendingIpToConfirm = ip
            AuthManager.save()

            val kickMsg = config.authKickMessage
                .replace("{bot}", config.botUsername)
                .replace("{code}", code)

            e.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(kickMsg)
            )
            return
        }

        // 3. Check group membership
        val isMember = runBlocking {
            try {
                val status = bridge.bot.getChatMember(config.authGroup, player.tgId!!).status
                status in listOf("creator", "administrator", "member", "restricted")
            } catch (ex: Exception) {
                bridge.logger.error("Failed to check group membership for ${player.tgId} in ${config.authGroup}", ex)
                false
            }
        }

        if (!isMember) {
            val kickMsg = config.authGroupKickMessage.replace("{group}", config.authGroup)
            e.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(kickMsg)
            )
            return
        }

        // 4. Check IP address
        if (!player.allowedIps.contains(ip)) {
            // New IP!
            // Check if we already sent a DM for this IP
            if (player.pendingIpToConfirm != ip) {
                player.pendingIpToConfirm = ip
                AuthManager.save()

                // Send Telegram DM with inline keyboard!
                runBlocking {
                    try {
                        val keyboard = TgInlineKeyboardMarkup(
                            listOf(
                                listOf(
                                    TgInlineKeyboardButton(
                                        text = "🟢 Подтвердить",
                                        callbackData = "ip_confirm:${player.username}:$ip",
                                        style = "success"
                                    ),
                                    TgInlineKeyboardButton(
                                        text = "🔴 Отклонить",
                                        callbackData = "ip_decline:${player.username}:$ip",
                                        style = "danger"
                                    )
                                )
                            )
                        )
                        val sentMsg = bridge.bot.sendMessageWithMarkup(
                            chatId = player.tgId!!,
                            text = "🔔 Обнаружен вход с нового IP: <code>$ip</code> для аккаунта <b>${player.username}</b>. Это вы?",
                            replyMarkup = keyboard,
                            parseMode = "HTML"
                        )
                        player.pendingIpMessageId = sentMsg.messageId
                        AuthManager.save()
                    } catch (ex: Exception) {
                        bridge.logger.error("Failed to send IP confirmation DM to ${player.tgId}", ex)
                    }
                }
            }

            val kickMsg = config.authIpKickMessage
            e.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(kickMsg)
            )
            return
        }
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
