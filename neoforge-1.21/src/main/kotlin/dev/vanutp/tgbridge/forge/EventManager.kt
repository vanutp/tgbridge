package dev.vanutp.tgbridge.forge

import com.mojang.brigadier.context.CommandContext
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
                    e.player.displayName?.string ?: return@addListener,
                    e.message.toAdventure()
                )
            )
        }
    }

    private fun registerPlayerDeathListener() {
        FORGE_BUS.addListener { e: LivingDeathEvent ->
            if (e.entity !is PlayerEntity) {
                return@addListener
            }
            val deathMessage = e.source.getDeathMessage(e.entity)
            NeoForgeTelegramBridge.onPlayerDeath(
                TBPlayerEventData(
                    e.entity.displayName?.string ?: return@addListener,
                    deathMessage.toAdventure(),
                )
            )
        }
    }

    private fun registerPlayerJoinListener() {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedInEvent ->
            NeoForgeTelegramBridge.onPlayerJoin(
                e.entity.displayName?.string ?: return@addListener
            )
        }
    }

    private fun registerPlayerLeaveListener() {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedOutEvent ->
            NeoForgeTelegramBridge.onPlayerLeave(
                e.entity.displayName?.string ?: return@addListener
            )
        }
    }

    private fun registerPlayerAdvancementListener() {
        FORGE_BUS.addListener { e: AdvancementEvent.AdvancementEarnEvent ->
            val advancement = e.advancement.value
            val display = advancement.display.getOrNull()
            if (display == null || !display.shouldAnnounceToChat() || e.entity.displayName == null) {
                return@addListener
            }
            val advancementTypeKey = "chat.type.advancement." + (display.frame?.name?.lowercase() ?: return@addListener)
            val advancementText =
                Text.translatable(advancementTypeKey, e.entity.displayName, advancement.name.get())
            NeoForgeTelegramBridge.onPlayerAdvancement(
                TBPlayerEventData(
                    e.entity.displayName!!.string,
                    advancementText.toAdventure(),
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
