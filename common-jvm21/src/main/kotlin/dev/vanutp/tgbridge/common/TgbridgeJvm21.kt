package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.modules.CarbonChatModule
import dev.vanutp.tgbridge.common.modules.FlectonePulseModule

object TgbridgeJvm21 {
    fun register(bridge: TelegramBridge) {
        bridge.addModule(CarbonChatModule(bridge))
        bridge.addModule(FlectonePulseModule(bridge))
    }
}
