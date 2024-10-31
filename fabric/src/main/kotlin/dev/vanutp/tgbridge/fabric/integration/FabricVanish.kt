package dev.vanutp.tgbridge.fabric.integration

import dev.vanutp.tgbridge.common.integration.Vanish
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import me.drex.vanish.api.VanishAPI
import me.drex.vanish.api.VanishEvents
import net.kyori.adventure.text.Component
import net.minecraft.entity.Entity
import net.minecraft.text.Text

object FabricVanish: Vanish() {

    override fun registerOnJoinMessage(handler: (TBPlayerEventData) -> Unit) {
        VanishEvents.VANISH_EVENT.register { player, vanish ->
            if (!vanish) handler.invoke(TBPlayerEventData(
                player.displayName?.literalString?:player.name.string,
                Component.empty()
            ))
        }
    }
    override fun registerOnLeaveMessage(handler: (TBPlayerEventData) -> Unit) {
        VanishEvents.VANISH_EVENT.register { player, vanish ->
            if (vanish) handler.invoke(TBPlayerEventData(
                player.displayName?.literalString?:player.name.string,
                Component.empty()
            ))
        }
    }
    override fun isVanished(player: Any): Boolean = VanishAPI.isVanished(player as Entity)

}