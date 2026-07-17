package dev.vanutp.tgbridge.forge

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.models.*
import net.kyori.adventure.text.Component
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraftforge.common.MinecraftForge.EVENT_BUS
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.AdvancementEvent
import net.minecraftforge.event.entity.player.PlayerEvent

class EventManager(private val tgbridge: ForgeTelegramBridge) {
    fun register() {
        registerChatMessageListener()
        registerPlayerDeathListener()
        registerPlayerJoinListener()
        registerPlayerLeaveListener()
        registerPlayerAdvancementListener()
        registerCommandHandlers()
    }

    private fun registerChatMessageListener() {
        EVENT_BUS.addListener { e: ServerChatEvent ->
            if (TelegramBridge.INSTANCE.chatModule != null) {
                return@addListener
            }
            tgbridge.onChatMessage(
                TgbridgeMcChatMessageEvent(
                    e.player.toTgbridge(),
                    Component.text(e.message),
                    null,
                    e,
                )
            )
        }
    }

    private fun registerPlayerDeathListener() {
        EVENT_BUS.addListener { e: LivingDeathEvent ->
            val player = e.entity
            if (player !is PlayerEntity) {
                return@addListener
            }
            val deathMessage = e.source.getDeathMessage(player)
            tgbridge.onPlayerDeath(
                TgbridgeDeathEvent(
                    player.toTgbridge(), deathMessage.toAdventure(), e,
                )
            )
        }
    }

    private fun registerPlayerJoinListener() {
        EVENT_BUS.addListener { e: PlayerEvent.PlayerLoggedInEvent ->
            val hasPlayedBefore = (e.player as IHasPlayedBefore).`tgbridge$getHasPlayedBefore`()
            tgbridge.onPlayerJoin(
                TgbridgeJoinEvent(e.player.toTgbridge(), hasPlayedBefore, e)
            )
        }
    }

    private fun registerPlayerLeaveListener() {
        EVENT_BUS.addListener { e: PlayerEvent.PlayerLoggedOutEvent ->
            tgbridge.onPlayerLeave(TgbridgeLeaveEvent(e.player.toTgbridge(), e))
        }
    }

    private fun registerPlayerAdvancementListener() {
        EVENT_BUS.addListener { e: AdvancementEvent ->
            val display = e.advancement.display
            if (display == null || !display.shouldAnnounceToChat()) {
                return@addListener
            }
            val type = display.frame?.id ?: return@addListener
            tgbridge.onPlayerAdvancement(
                TgbridgeAdvancementEvent(
                    e.player.toTgbridge(),
                    type,
                    display.title.toAdventure(),
                    display.description.toAdventure(),
                    e,
                )
            )
        }
    }

    private fun onReloadCommand(ctx: CommandContext<ServerCommandSource>): Int {
        val res = tgbridge.onReloadCommand(ctx.toTgbridge())
        return if (res) 1 else -1
    }

    private fun onToggleMuteCommand(ctx: CommandContext<ServerCommandSource>): Int {
        val res = tgbridge.onToggleMuteCommand(ctx.toTgbridge())
        return if (res) 1 else -1
    }

    private fun onSendCommand(ctx: CommandContext<ServerCommandSource>): Int {
        val format = ctx.nodes[2].node.name
        val chatName = StringArgumentType.getString(ctx, "chatName")
        val message = StringArgumentType.getString(ctx, "message")
        val res = tgbridge.onSendCommand(ctx.toTgbridge(), format, chatName, message)
        return if (res) 1 else -1
    }

    private fun registerCommandHandlers() {
        // TODO: get rid of code duplication between versions and loaders
        val appendSendArgs = { builder: ArgumentBuilder<ServerCommandSource, *> ->
            builder.then(
                CommandManager.argument("chatName", StringArgumentType.string()).then(
                    CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(::onSendCommand)
                )
            )
        }
        EVENT_BUS.addListener { e: RegisterCommandsEvent ->
            e.dispatcher.register(
                CommandManager.literal("tgbridge")
                    .then(
                        CommandManager.literal("reload")
                            .requires { it.hasPermissionLevel(4) }
                            .executes(::onReloadCommand)
                    )
                    .then(
                        CommandManager.literal("toggle")
                            .executes(::onToggleMuteCommand)
                    )
                    .then(
                        CommandManager.literal("send")
                            .requires { it.hasPermissionLevel(2) }
                            .then(appendSendArgs(CommandManager.literal("plain")))
                            .then(appendSendArgs(CommandManager.literal("mm")))
                            .then(appendSendArgs(CommandManager.literal("html")))
                            .then(appendSendArgs(CommandManager.literal("json")))
                    )
            )
        }
    }
}
