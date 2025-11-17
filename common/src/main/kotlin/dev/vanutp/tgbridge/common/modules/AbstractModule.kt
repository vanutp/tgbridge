package dev.vanutp.tgbridge.common.modules

import dev.vanutp.tgbridge.common.TelegramBridge

abstract class AbstractModule(protected open val bridge: TelegramBridge) : ITgbridgeModule {
    open val fabricId: String? = null
    open val forgeId: String? = null
    open val paperId: String? = null

    override fun shouldEnable() =
        listOf(fabricId, forgeId, paperId).all { it == null }
                || bridge.platform.isModLoadedMulti(fabricId, forgeId, paperId)
}
