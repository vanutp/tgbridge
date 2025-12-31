package dev.vanutp.tgbridge.fabric

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.models.*
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.PlayerChatMessage
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.PermissionLevel
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

    private fun onSendCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val format = ctx.nodes[2].node.name
        val chatName = StringArgumentType.getString(ctx, "chatName")
        val message = StringArgumentType.getString(ctx, "message")
        val res = FabricTelegramBridge.onSendCommand(ctx.toTgbridge(), format, chatName, message)
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
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                Commands.literal("tgbridge")
                    .then(
                        Commands.literal("reload")
                            .requires { it.hasOwnerPermission() }
                            .executes(::onReloadCommand)
                    )
                    .then(
                        Commands.literal("toggle")
                            .executes(::onToggleMuteCommand)
                    )
                    .then(
                        Commands.literal("send")
                            .requires { it.hasOwnerPermission() }
                            .then(appendSendArgs(Commands.literal("plain")))
                            .then(appendSendArgs(Commands.literal("mm")))
                            .then(appendSendArgs(Commands.literal("html")))
                            .then(appendSendArgs(Commands.literal("json")))
                    )
            )
        }
    }

    private fun CommandSourceStack.hasOwnerPermission(): Boolean =
        if (FabricTelegramBridge.versionInfo.IS_2111) {
            permissions().hasPermission(Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))
        } else {
            val cls = this.javaClass
            val hasPermission = cls.getMethod("method_9259", Int::class.javaPrimitiveType)
            hasPermission.invoke(this, 2) as Boolean
        }
}
