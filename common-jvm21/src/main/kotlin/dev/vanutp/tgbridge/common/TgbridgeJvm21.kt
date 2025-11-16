package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.compat.CarbonChatCompat
import dev.vanutp.tgbridge.common.compat.FlectonePulseCompat

object TgbridgeJvm21 {
    fun register(bridge: TelegramBridge) {
        bridge.addIntegration(CarbonChatCompat(bridge))
        bridge.addIntegration(FlectonePulseCompat(bridge))
    }
}
