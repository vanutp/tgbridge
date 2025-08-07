package dev.vanutp.tgbridge.fabric

import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.MutedUsers
import dev.vanutp.tgbridge.common.models.*
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

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
        ServerMessageEvents.CHAT_MESSAGE.register { message: SignedMessage, sender, params ->
            val messageContent = if (FabricTelegramBridge.versionInfo.IS_192) {
                val cls = message.javaClass
                val getContent = cls.getMethod("method_44125")
                getContent.invoke(message) as Text
            } else {
                message.content
            }
            FabricTelegramBridge.onChatMessage(
                TgbridgeMcChatMessageEvent(
                    sender.toTgbridge(),
                    messageContent.toAdventure(),
                    FabricEventWrapper(
                        ServerMessageEvents.ChatMessage::class,
                        listOf(message, sender, params),
                    ),
                )
            )
        }
    }

    private fun registerPlayerDeathListener() {
        CustomEvents.PLAYER_DEATH_EVENT.register { player, damageSource ->
            val deathMessage = damageSource.getDeathMessage(player)
            FabricTelegramBridge.onPlayerDeath(
                TgbridgeDeathEvent(
                    player.toTgbridge(),
                    deathMessage.toAdventure(),
                    FabricEventWrapper(
                        CustomEvents.PlayerDeath::class, listOf(player, damageSource)
                    ),
                )
            )
        }
    }

    private fun registerPlayerJoinListener() {
        CustomEvents.PLAYER_JOIN_EVENT.register { player, hasPlayedBefore ->
            FabricTelegramBridge.onPlayerJoin(
                TgbridgeJoinEvent(
                    player.toTgbridge(),
                    hasPlayedBefore,
                    FabricEventWrapper(
                        CustomEvents.PlayerJoin::class, listOf(player, hasPlayedBefore)
                    ),
                )
            )
        }
    }

    private fun registerPlayerLeaveListener() {
        CustomEvents.PLAYER_LEAVE_EVENT.register { player ->
            FabricTelegramBridge.onPlayerLeave(
                TgbridgeLeaveEvent(
                    player.toTgbridge(),
                    FabricEventWrapper(CustomEvents.PlayerLeave::class, listOf(player)),
                )
            )
        }
    }

    private fun registerPlayerAdvancementListener() {
        CustomEvents.ADVANCEMENT_EARN_EVENT.register { player, display ->
            if (!display.shouldAnnounceToChat()) {
                return@register
            }
            val type = display.frame?.name?.lowercase() ?: return@register
            FabricTelegramBridge.onPlayerAdvancement(
                TgbridgeAdvancementEvent(
                    player.toTgbridge(),
                    type,
                    display.title.toAdventure(),
                    display.description.toAdventure(),
                    FabricEventWrapper(CustomEvents.AdvancementEarn::class, listOf(player, display)),
                )
            )
        }
    }

    private fun onReloadCommand(ctx: CommandContext<ServerCommandSource>): Int {
        val res = FabricTelegramBridge.onReloadCommand(
            TBCommandContext(
                reply = { text ->
                    val textComponent = Text.literal(text)
                    if (FabricTelegramBridge.versionInfo.IS_19) {
                        val cls = ctx.source.javaClass
                        val sendFeedback = cls.getMethod(
                            "method_9226",
                            Text::class.java,
                            Boolean::class.javaPrimitiveType
                        )
                        sendFeedback.invoke(ctx.source, textComponent, false)
                    } else {
                        ctx.source.sendFeedback({ textComponent }, false)
                    }
                }
            ))
        return if (res) 1 else -1
    }

    private fun registerCommandHandlers() {
        // TODO: get rid of code duplication between versions and loaders
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("tgbridge").then(
                    CommandManager.literal("reload")
                        .requires { it.hasPermissionLevel(4) }
                        .executes(::onReloadCommand)
                )
            )
            dispatcher.register(
                CommandManager.literal("tgshow")
                    .executes {
                        val player = it.source.player?.uuid ?: return@executes -1
                        MutedUsers.unmute(player)
                        return@executes 1
                    }
            )
            dispatcher.register(
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
