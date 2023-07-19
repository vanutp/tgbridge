package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.TelegramBridgeBase

class TelegramBridge(private val plugin: TelegramBridgeBootstrap) : TelegramBridgeBase() {
    override val logger = Logger(plugin)
    override val platform: String = "paper"

}
