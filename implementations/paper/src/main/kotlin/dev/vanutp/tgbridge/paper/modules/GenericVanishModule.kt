package dev.vanutp.tgbridge.paper.modules

import dev.vanutp.tgbridge.common.modules.IVanishModule
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import dev.vanutp.tgbridge.paper.PaperTelegramBridge

class GenericVanishModule(bridge: PaperTelegramBridge) : AbstractPaperModule(bridge), IVanishModule {
    override fun isVanished(player: ITgbridgePlayer) =
        bridge.plugin.server
            .getPlayer(player.uuid)
            ?.getMetadata("vanished")
            ?.any { it.asBoolean() }
            ?: false
}
