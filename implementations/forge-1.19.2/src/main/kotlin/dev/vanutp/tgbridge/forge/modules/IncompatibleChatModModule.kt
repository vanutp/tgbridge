package dev.vanutp.tgbridge.forge.modules

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import dev.vanutp.tgbridge.common.modules.AbstractModule
import dev.vanutp.tgbridge.common.modules.IChatModule
import dev.vanutp.tgbridge.forge.ForgeTelegramBridge
import dev.vanutp.tgbridge.forge.toAdventure
import dev.vanutp.tgbridge.forge.toTgbridge
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.server.ServerLifecycleHooks
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

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
                e.message.toAdventure(),
                null,
                e,
            )
        )
    }

    override fun enable() {
        FORGE_BUS.register(this)
        TgbridgeEvents.RECIPIENTS.addListener { e ->
            e.recipients += getChatRecipients(e.chat) ?: emptyList()
        }
    }

    override fun disable() {
        FORGE_BUS.unregister(this)
    }

    fun getChatRecipients(chat: ChatConfig): List<ITgbridgePlayer>? =
        ServerLifecycleHooks.getCurrentServer().playerList.players
            .takeIf { chat.isDefault }
            ?.map { it.toTgbridge() }
}
