package dev.vanutp.tgbridge.fabric

import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
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
        ServerMessageEvents.CHAT_MESSAGE.register { message: SignedMessage, sender, _ ->
            val messageContent = if (FabricTelegramBridge.versionInfo.IS_192) {
                val cls = message.javaClass
                val getContent = cls.getMethod("method_44125")
                getContent.invoke(message) as Text
            } else {
                message.content
            }
            FabricTelegramBridge.onChatMessage(
                TBPlayerEventData(
                    sender.displayName?.string ?: return@register,
                    messageContent.toAdventure(),
                )
            )
        }
    }

    private fun registerPlayerDeathListener() {
        CustomEvents.PLAYER_DEATH_EVENT.register { player, damageSource ->
            if (player.isVanished()) {
                return@register
            }
            val deathMessage = damageSource.getDeathMessage(player)
            FabricTelegramBridge.onPlayerDeath(
                TBPlayerEventData(
                    player.displayName?.string ?: return@register,
                    deathMessage.toAdventure(),
                )
            )
        }
    }

    private fun registerPlayerJoinListener() {
        CustomEvents.PLAYER_JOIN_EVENT.register { player ->
            if (player.isVanished()) {
                return@register
            }
            FabricTelegramBridge.onPlayerJoin(
                player.displayName?.string ?: return@register,
            )
        }
    }

    private fun registerPlayerLeaveListener() {
        CustomEvents.PLAYER_LEAVE_EVENT.register { player ->
            if (player.isVanished()) {
                return@register
            }
            FabricTelegramBridge.onPlayerLeave(
                player.displayName?.string ?: return@register,
            )
        }
    }

    private fun registerPlayerAdvancementListener() {
        CustomEvents.ADVANCEMENT_EARN_EVENT.register { player, advancementType, advancementNameComponent ->
            if (player.displayName == null || player.isVanished()) {
                return@register
            }
            val advancementTypeKey = "chat.type.advancement.$advancementType"
            val advancementText =
                Text.translatable(advancementTypeKey, player.displayName, advancementNameComponent)
            FabricTelegramBridge.onPlayerAdvancement(
                TBPlayerEventData(
                    player.displayName!!.string,
                    advancementText.toAdventure()
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
        }
    }
}
