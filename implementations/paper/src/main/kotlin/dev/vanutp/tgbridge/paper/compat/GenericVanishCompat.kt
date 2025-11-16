package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.compat.IVanishCompat
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import dev.vanutp.tgbridge.paper.PaperTelegramBridge

class GenericVanishCompat(bridge: PaperTelegramBridge) : AbstractPaperCompat(bridge), IVanishCompat {
    override fun isVanished(player: ITgbridgePlayer) =
        bridge.plugin.server
            .getPlayer(player.uuid)
            ?.getMetadata("vanished")
            ?.any { it.asBoolean() }
            ?: false
}
