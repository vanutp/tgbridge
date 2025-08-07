package dev.vanutp.tgbridge.forge

import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.MutedUsers
import dev.vanutp.tgbridge.common.models.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
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
            ForgeTelegramBridge.onChatMessage(
                TgbridgeMcChatMessageEvent(
                    e.player.toTgbridge(), e.message.toAdventure(), e,
                )
            )
        }
    }

    private fun registerPlayerDeathListener() {
        FORGE_BUS.addListener { e: LivingDeathEvent ->
            val player = e.entity
            if (player !is PlayerEntity) {
                return@addListener
            }
            val deathMessage = e.source.getDeathMessage(player)
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
            if (display == null || !display.shouldAnnounceToChat()) {
                return@addListener
            }
            val type = display.frame?.id ?: return@addListener
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

    private fun onReloadCommand(ctx: CommandContext<ServerCommandSource>): Int {
        val res = ForgeTelegramBridge.onReloadCommand(
            TBCommandContext(
                reply = { text ->
                    ctx.source.sendFeedback(Text.literal(text), false)
                }
            ))
        return if (res) 1 else -1
    }

    private fun registerCommandHandlers() {
        // TODO: get rid of code duplication between versions and loaders
        FORGE_BUS.addListener { e: RegisterCommandsEvent ->
            e.dispatcher.register(
                CommandManager.literal("tgbridge").then(
                    CommandManager.literal("reload")
                        .requires { it.hasPermissionLevel(4) }
                        .executes(::onReloadCommand)
                )
            )
            e.dispatcher.register(
                CommandManager.literal("tgshow")
                    .executes {
                        val player = it.source.player?.uuid ?: return@executes -1
                        MutedUsers.unmute(player)
                        return@executes 1
                    }
            )
            e.dispatcher.register(
                CommandManager.literal("tghide")
                    .executes {
                        val player = it.source.player?.uuid ?: return@executes -1
                        MutedUsers.mute(player)
                        return@executes 1
                    }
            )
        }
    }
}
