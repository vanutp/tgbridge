package dev.vanutp.tgbridge.common.compat

import dev.vanutp.tgbridge.common.models.TgbridgePlayer

interface IVanishCompat {
    fun isVanished(player: TgbridgePlayer): Boolean
}
