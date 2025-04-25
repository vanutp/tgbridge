package dev.vanutp.tgbridge.forge

import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.models.TBAdvancementEvent
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import net.kyori.adventure.text.Component
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
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
            tgbridge.onChatMessage(
                TBPlayerEventData(
                    getPlayerName(e.player).string,
                    Component.text(e.message),
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
                TBPlayerEventData(
                    getPlayerName(player).string,
                    deathMessage.toAdventure(),
                )
            )
        }
    }

    private fun registerPlayerJoinListener() {
        EVENT_BUS.addListener { e: PlayerEvent.PlayerLoggedInEvent ->
            val hasPlayedBefore = e.player.persistentData.run {
                getBoolean("hasPlayedBefore").also {
                    putBoolean("hasPlayedBefore", true)
                }
            }
            tgbridge.onPlayerJoin(
                getPlayerName(e.player).string,
                hasPlayedBefore,
            )
        }
    }

    private fun registerPlayerLeaveListener() {
        EVENT_BUS.addListener { e: PlayerEvent.PlayerLoggedOutEvent ->
            tgbridge.onPlayerLeave(getPlayerName(e.player).string)
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
                TBAdvancementEvent(
                    getPlayerName(e.player).string,
                    type,
                    display.title.toAdventure(),
                    display.description.toAdventure(),
                )
            )
        }
    }

    private fun onReloadCommand(ctx: CommandContext<ServerCommandSource>): Int {
        val res = tgbridge.onReloadCommand(
            TBCommandContext(
                reply = { text ->
                    ctx.source.sendFeedback(LiteralText(text), false)
                }
            ))
        return if (res) 1 else -1
    }

    private fun registerCommandHandlers() {
        // TODO: get rid of code duplication between versions and loaders
        EVENT_BUS.addListener { e: RegisterCommandsEvent ->
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
