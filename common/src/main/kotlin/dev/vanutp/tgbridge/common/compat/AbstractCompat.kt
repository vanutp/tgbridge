package dev.vanutp.tgbridge.common.compat

import dev.vanutp.tgbridge.common.TelegramBridge

abstract class AbstractCompat(protected open val bridge: TelegramBridge) : ITgbridgeCompat {
    open val fabricId: String? = null
    open val forgeId: String? = null
    open val paperId: String? = null

    override fun shouldEnable() =
        listOf(fabricId, forgeId, paperId).all { it == null }
                || bridge.platform.isModLoadedMulti(fabricId, forgeId, paperId)
}
