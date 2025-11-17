package dev.vanutp.tgbridge.fabric

import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.models.*
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.network.chat.Component as Text


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
        ServerMessageEvents.CHAT_MESSAGE.register { message: PlayerChatMessage, sender, params ->
            if (TelegramBridge.INSTANCE.chatModule != null) {
                return@register
            }
            val messageContent = if (FabricTelegramBridge.versionInfo.IS_192) {
                val cls = message.javaClass
                val getContent = cls.getMethod("method_44125")
                getContent.invoke(message) as Text
            } else {
                message.decoratedContent()
            }
            FabricTelegramBridge.onChatMessage(
                TgbridgeMcChatMessageEvent(
                    sender.toTgbridge(),
                    messageContent.toAdventure(),
                    null,
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
            val deathMessage = damageSource.getLocalizedDeathMessage(player)
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
            if (!display.shouldAnnounceChat()) {
                return@register
            }
            val type = display.type?.name?.lowercase() ?: return@register
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

    private fun onReloadCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val res = FabricTelegramBridge.onReloadCommand(ctx.toTgbridge())
        return if (res) 1 else -1
    }

    private fun onToggleMuteCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val res = FabricTelegramBridge.onToggleMuteCommand(ctx.toTgbridge())
        return if (res) 1 else -1
    }

    private fun registerCommandHandlers() {
        // TODO: get rid of code duplication between versions and loaders
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
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
