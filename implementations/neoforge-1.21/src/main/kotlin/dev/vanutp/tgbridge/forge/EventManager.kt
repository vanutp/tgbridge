package dev.vanutp.tgbridge.forge

import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.models.TBAdvancementEvent
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.ServerChatEvent
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent
import net.neoforged.neoforge.event.entity.player.AdvancementEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import kotlin.jvm.optionals.getOrNull

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
            NeoForgeTelegramBridge.onChatMessage(
                TBPlayerEventData(
                    getPlayerName(e.player).string,
                    e.message.toAdventure()
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
            NeoForgeTelegramBridge.onPlayerDeath(
                TBPlayerEventData(
                    getPlayerName(player).string,
                    deathMessage.toAdventure(),
                )
            )
        }
    }

    private fun registerPlayerJoinListener() {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedInEvent ->
            val hasPlayedBefore = e.entity.persistentData.run {
                getBoolean("hasPlayedBefore").also {
                    putBoolean("hasPlayedBefore", true)
                }
            }
            NeoForgeTelegramBridge.onPlayerJoin(
                getPlayerName(e.entity).string,
                hasPlayedBefore,
            )
        }
    }

    private fun registerPlayerLeaveListener() {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedOutEvent ->
            NeoForgeTelegramBridge.onPlayerLeave(
                getPlayerName(e.entity).string,
            )
        }
    }

    private fun registerPlayerAdvancementListener() {
        FORGE_BUS.addListener { e: AdvancementEvent.AdvancementEarnEvent ->
            val advancement = e.advancement.value
            val display = advancement.display.getOrNull()
            if (display == null || !display.shouldAnnounceToChat()) {
                return@addListener
            }
            val type = display.frame?.name?.lowercase() ?: return@addListener
            NeoForgeTelegramBridge.onPlayerAdvancement(
                TBAdvancementEvent(
                    getPlayerName(e.entity).string,
                    type,
                    display.title.toAdventure(),
                    display.description.toAdventure(),
                )
            )
        }
    }

    private fun onReloadCommand(ctx: CommandContext<ServerCommandSource>): Int {
        val res = NeoForgeTelegramBridge.onReloadCommand(
            TBCommandContext(
                reply = { text ->
                    ctx.source.sendFeedback({ Text.literal(text) }, false)
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
        }
    }
}
