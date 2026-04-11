package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.modules.FlectonePulseModule

object TgbridgeJvm25 {
    fun register(bridge: TelegramBridge) {
        bridge.addModule(FlectonePulseModule(bridge))
    }
}
