package dev.vanutp.tgbridge.common.compat

import dev.vanutp.tgbridge.common.models.ITgbridgePlayer

interface IVanishCompat {
    fun isVanished(player: ITgbridgePlayer): Boolean
}
