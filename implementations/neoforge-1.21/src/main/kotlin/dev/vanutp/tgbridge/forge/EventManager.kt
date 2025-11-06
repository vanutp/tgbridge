package dev.vanutp.tgbridge.forge

import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.models.*
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.world.entity.player.Player
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
                TgbridgeMcChatMessageEvent(
                    e.player.toTgbridge(), e.message.toAdventure(), e,
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
            NeoForgeTelegramBridge.onPlayerDeath(
                TgbridgeDeathEvent(
                    player.toTgbridge(), deathMessage.toAdventure(), e,
                )
            )
        }
    }

    private fun registerPlayerJoinListener() {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedInEvent ->
            val hasPlayedBefore = (e.entity as IHasPlayedBefore).`tgbridge$getHasPlayedBefore`()
            NeoForgeTelegramBridge.onPlayerJoin(
                TgbridgeJoinEvent(e.entity.toTgbridge(), hasPlayedBefore, e)
            )
        }
    }

    private fun registerPlayerLeaveListener() {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedOutEvent ->
            NeoForgeTelegramBridge.onPlayerLeave(TgbridgeLeaveEvent(e.entity.toTgbridge(), e))
        }
    }

    private fun registerPlayerAdvancementListener() {
        FORGE_BUS.addListener { e: AdvancementEvent.AdvancementEarnEvent ->
            val advancement = e.advancement.value
            val display = advancement.display.getOrNull()
            if (display == null || !display.shouldAnnounceChat()) {
                return@addListener
            }
            val type = display.type.name.lowercase()
            NeoForgeTelegramBridge.onPlayerAdvancement(
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
        val res = NeoForgeTelegramBridge.onReloadCommand(ctx.toTgbridge())
        return if (res) 1 else -1
    }

    private fun onToggleMuteCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val res = NeoForgeTelegramBridge.onToggleMuteCommand(ctx.toTgbridge())
        return if (res) 1 else -1
    }

    private fun registerCommandHandlers() {
        // TODO: get rid of code duplication between versions and loaders
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
            )
        }
    }
}
