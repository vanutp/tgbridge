package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.TelegramBridge

class ForgeTelegramBridge : TelegramBridge() {
    companion object {
        const val MOD_ID = "tgbridge"
    }

    override val logger = ForgeLogger()
    override val platform = ForgePlatform()
}
