package dev.vanutp.tgbridge.forge

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.models.*
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.world.entity.player.Player
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.AdvancementEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

object EventManager {
    fun register() {
        registerChatMessageListener()
        registerPlayerDeathListener()
        registerPlayerJoinListener()
        registerPlayerLeaveListener()
        registerPlayerAdvancementListener()
        registerCommandHandlers()
    }

    private fun registerChatMessageListener() {
        FORGE_BUS.addListener { e: ServerChatEvent ->
            if (TelegramBridge.INSTANCE.chatModule != null) {
                return@addListener
            }
            ForgeTelegramBridge.onChatMessage(
                TgbridgeMcChatMessageEvent(
                    e.player.toTgbridge(),
                    e.message.toAdventure(),
                    null,
                    e,
                )
            )
        }
    }

    private fun registerPlayerDeathListener() {
        FORGE_BUS.addListener { e: LivingDeathEvent ->
            val player = e.entity
            if (player !is Player) {
                return@addListener
            }
            val deathMessage = e.source.getLocalizedDeathMessage(player)
            ForgeTelegramBridge.onPlayerDeath(
                TgbridgeDeathEvent(
                    player.toTgbridge(), deathMessage.toAdventure(), e,
                )
            )
        }
    }

    private fun registerPlayerJoinListener() {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedInEvent ->
            val hasPlayedBefore = (e.entity as IHasPlayedBefore).`tgbridge$getHasPlayedBefore`()
            ForgeTelegramBridge.onPlayerJoin(
                TgbridgeJoinEvent(e.entity.toTgbridge(), hasPlayedBefore, e)
            )
        }
    }

    private fun registerPlayerLeaveListener() {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedOutEvent ->
            ForgeTelegramBridge.onPlayerLeave(TgbridgeLeaveEvent(e.entity.toTgbridge(), e))
        }
    }

    private fun registerPlayerAdvancementListener() {
        FORGE_BUS.addListener { e: AdvancementEvent.AdvancementEarnEvent ->
            val display = e.advancement.display
            if (display == null || !display.shouldAnnounceChat()) {
                return@addListener
            }
            val type = display.frame.getName()
            ForgeTelegramBridge.onPlayerAdvancement(
                TgbridgeAdvancementEvent(
                    e.entity.toTgbridge(),
                    type,
                    display.title.toAdventure(),
                    display.description.toAdventure(),
                    e,
                )
            )
        }
    }

    private fun onReloadCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val res = ForgeTelegramBridge.onReloadCommand(ctx.toTgbridge())
        return if (res) 1 else -1
    }

    private fun onToggleMuteCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val res = ForgeTelegramBridge.onToggleMuteCommand(ctx.toTgbridge())
        return if (res) 1 else -1
    }

    private fun onSendCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val format = ctx.nodes[2].node.name
        val chatName = StringArgumentType.getString(ctx, "chatName")
        val message = StringArgumentType.getString(ctx, "message")
        val res = ForgeTelegramBridge.onSendCommand(ctx.toTgbridge(), format, chatName, message)
        return if (res) 1 else -1
    }

    private fun registerCommandHandlers() {
        // TODO: get rid of code duplication between versions and loaders
        val appendSendArgs = { builder: ArgumentBuilder<CommandSourceStack, *> ->
            builder.then(
                Commands.argument("chatName", StringArgumentType.string()).then(
                    Commands.argument("message", StringArgumentType.greedyString())
                        .executes(::onSendCommand)
                )
            )
        }
        FORGE_BUS.addListener { e: RegisterCommandsEvent ->
            e.dispatcher.register(
                Commands.literal("tgbridge")
                    .then(
                        Commands.literal("reload")
                            .requires { it.hasPermission(4) }
                            .executes(::onReloadCommand)
                    )
                    .then(
                        Commands.literal("toggle")
                            .executes(::onToggleMuteCommand)
                    )
                    .then(
                        Commands.literal("send")
                            .requires { it.hasPermission(2) }
                            .then(appendSendArgs(Commands.literal("plain")))
                            .then(appendSendArgs(Commands.literal("mm")))
                            .then(appendSendArgs(Commands.literal("html")))
                            .then(appendSendArgs(Commands.literal("json")))
                    )
            )
        }
    }
}
