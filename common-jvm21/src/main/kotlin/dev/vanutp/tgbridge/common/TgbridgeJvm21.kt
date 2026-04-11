package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.modules.CarbonChatModule

object TgbridgeJvm21 {
    fun register(bridge: TelegramBridge) {
        bridge.addModule(CarbonChatModule(bridge))
    }
}
