package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.TelegramBridge
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.world.World
import net.minecraftforge.event.ServerChatEvent
import net.minecraftforge.server.ServerLifecycleHooks
import thedarkcolour.kotlinforforge.forge.FORGE_BUS

class ForgeTelegramBridge : TelegramBridge() {
    companion object {
        const val MOD_ID = "tgbridge"
    }

    override val logger = ForgeLogger()
    override val platform = ForgePlatform()

    init {
    }
}
