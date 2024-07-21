package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.TelegramBridge

class NeoForgeTelegramBridge : TelegramBridge() {
    companion object {
        const val MOD_ID = "tgbridge"
    }

    override val logger = NeoForgeLogger()
    override val platform = NeoForgePlatform()
}
