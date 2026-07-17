package dev.vanutp.tgbridge.forge.modules

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.modules.AbstractModule
import dev.vanutp.tgbridge.common.modules.IChatModule
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.forge.NeoForgeTelegramBridge
import dev.vanutp.tgbridge.forge.toAdventure
import dev.vanutp.tgbridge.forge.toTgbridge
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.ServerChatEvent
import net.neoforged.neoforge.server.ServerLifecycleHooks

class IncompatibleChatModModule(override val bridge: NeoForgeTelegramBridge) : AbstractModule(bridge), IChatModule {
    override val canBeDisabled = true

    override fun shouldEnable(): Boolean {
        return config.integrations.incompatiblePluginChatPrefix != null
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onMessage(e: ServerChatEvent) {
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                e.player.toTgbridge(),
                e.message.toAdventure(),
                null,
                e,
            )
        )
    }

    override fun enable() {
        NeoForge.EVENT_BUS.register(this)
        TgbridgeEvents.RECIPIENTS.addListener { e ->
            e.recipients += getChatRecipients(e.chat) ?: emptyList()
        }
    }

    override fun disable() {
        NeoForge.EVENT_BUS.unregister(this)
    }

    fun getChatRecipients(chat: ChatConfig) =
        ServerLifecycleHooks.getCurrentServer()?.playerList?.players
            ?.takeIf { chat.isDefault }
            ?.map { it.toTgbridge() }
}
