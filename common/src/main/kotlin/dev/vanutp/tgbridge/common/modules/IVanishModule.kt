package dev.vanutp.tgbridge.common.modules

import dev.vanutp.tgbridge.common.models.ITgbridgePlayer

interface IVanishModule {
    fun isVanished(player: ITgbridgePlayer): Boolean
}
