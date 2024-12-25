package dev.vanutp.tgbridge.forge

import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
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
                TBPlayerEventData(
                    e.player.displayName.string,
                    e.message.toAdventure(),
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
            ForgeTelegramBridge.onPlayerDeath(
                TBPlayerEventData(
                    e.entity.displayName.string,
                    deathMessage.toAdventure(),
                )
            )
        }
    }

    private fun registerPlayerJoinListener() {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedInEvent ->
            ForgeTelegramBridge.onPlayerJoin(e.entity.displayName.string)
        }
    }

    private fun registerPlayerLeaveListener() {
        FORGE_BUS.addListener { e: PlayerEvent.PlayerLoggedOutEvent ->
            ForgeTelegramBridge.onPlayerLeave(e.entity.displayName.string)
        }
    }

    private fun registerPlayerAdvancementListener() {
        FORGE_BUS.addListener { e: AdvancementEvent.AdvancementEarnEvent ->
            val display = e.advancement.display
            if (display == null || !display.shouldAnnounceToChat()) {
                return@addListener
            }
            val advancementTypeKey = "chat.type.advancement." + (display.frame?.id ?: return@addListener)
            val advancementText =
                Text.translatable(advancementTypeKey, e.entity.displayName, e.advancement.toHoverableText())
            ForgeTelegramBridge.onPlayerAdvancement(
                TBPlayerEventData(
                    e.entity.displayName.string,
                    advancementText.toAdventure(),
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
        }
    }
}
