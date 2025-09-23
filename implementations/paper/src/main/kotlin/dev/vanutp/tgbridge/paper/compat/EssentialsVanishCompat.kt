package dev.vanutp.tgbridge.paper.compat

import com.earth2me.essentials.Essentials
import dev.vanutp.tgbridge.common.EventResult
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.models.TgbridgeJoinEvent
import dev.vanutp.tgbridge.common.models.TgbridgeLeaveEvent
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import dev.vanutp.tgbridge.paper.toTgbridge
import net.ess3.api.events.VanishStatusChangeEvent
import net.essentialsx.api.v2.events.AsyncUserDataLoadEvent
import org.bukkit.event.EventHandler

class EssentialsVanishCompat(bridge: PaperTelegramBridge) :
    AbstractPaperCompat(bridge) {
    override val paperId = "Essentials"

    @EventHandler
    fun onPlayerJoin(e: AsyncUserDataLoadEvent) {
        if (!e.user.isHidden) {
            bridge.onPlayerJoin(
                TgbridgeJoinEvent(
                    e.user.base.toTgbridge(),
                    e.user.base.hasPlayedBefore(),
                    e,
                )
            )
        }
    }

    override fun enable() {
        super.enable()
        TgbridgeEvents.JOIN.addListener { e ->
            if (e.originalEvent is AsyncUserDataLoadEvent || e.originalEvent is VanishStatusChangeEvent) {
                EventResult.CONTINUE
            } else {
                EventResult.STOP
            }
        }
        TgbridgeEvents.LEAVE.addListener { e ->
            if (e.originalEvent is VanishStatusChangeEvent) {
                return@addListener EventResult.CONTINUE
            }
            val ess = bridge.plugin.server.pluginManager.getPlugin(paperId) as Essentials
            val user = ess.getUser(e.player.uuid) ?: return@addListener EventResult.CONTINUE
            if (user.isHidden || user.isLeavingHidden) {
                EventResult.STOP
            } else {
                EventResult.CONTINUE
            }
        }
    }

    @EventHandler
    fun onVanishStatusChange(e: VanishStatusChangeEvent) {
        val player = e.affected.base.toTgbridge()
        if (e.value) {
            bridge.onPlayerLeave(TgbridgeLeaveEvent(player, e, ignoreVanish = true))
        } else {
            bridge.onPlayerJoin(TgbridgeJoinEvent(player, true, e, ignoreVanish = true))
        }
    }
}
