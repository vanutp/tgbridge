package dev.vanutp.tgbridge.forge.compat

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.modules.AbstractModule
import dev.vanutp.tgbridge.common.modules.IChatModule
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.forge.ForgeTelegramBridge
import dev.vanutp.tgbridge.forge.toTgbridge
import net.kyori.adventure.text.Component
import net.minecraftforge.common.MinecraftForge.EVENT_BUS
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.server.ServerLifecycleHooks

class IncompatibleChatModModule(override val bridge: ForgeTelegramBridge) : AbstractModule(bridge), IChatModule {
    override val canBeDisabled = true

    override fun shouldEnable(): Boolean {
        return config.integrations.incompatiblePluginChatPrefix != null
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onMessage(e: ServerChatEvent) {
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                e.player.toTgbridge(),
                Component.text(e.message),
                null,
                e,
            )
        )
    }

    override fun enable() {
        EVENT_BUS.register(this)
        TgbridgeEvents.RECIPIENTS.addListener { e ->
            e.recipients += getChatRecipients(e.chat) ?: emptyList()
        }
    }

    override fun disable() {
        EVENT_BUS.unregister(this)
    }

    fun getChatRecipients(chat: ChatConfig): List<ITgbridgePlayer>? =
        ServerLifecycleHooks.getCurrentServer().playerManager.playerList
            .takeIf { chat.isDefault }
            ?.map { it.toTgbridge() }
}
