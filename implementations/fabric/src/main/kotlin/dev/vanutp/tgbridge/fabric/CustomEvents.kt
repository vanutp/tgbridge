package dev.vanutp.tgbridge.fabric

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.advancement.AdvancementDisplay
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity


class CustomEvents {
    companion object {
        val ADVANCEMENT_EARN_EVENT = EventFactory.createArrayBacked(AdvancementEarn::class.java) { handlers ->
            AdvancementEarn { player, display ->
                for (handler in handlers) {
                    handler.onAdvancementEarn(player, display)
                }
            }
        }
        val PLAYER_JOIN_EVENT = EventFactory.createArrayBacked(PlayerJoin::class.java) { handlers ->
            PlayerJoin { player, hasPlayedBefore ->
                for (handler in handlers) {
                    handler.onPlayerJoin(player, hasPlayedBefore)
                }
            }
        }
        val PLAYER_LEAVE_EVENT = EventFactory.createArrayBacked(PlayerLeave::class.java) { handlers ->
            PlayerLeave { player ->
                for (handler in handlers) {
                    handler.onPlayerLeave(player)
                }
            }
        }
        val PLAYER_DEATH_EVENT = EventFactory.createArrayBacked(PlayerDeath::class.java) { handlers ->
            PlayerDeath { player, damageSource ->
                for (handler in handlers) {
                    handler.onPlayerDeath(player, damageSource)
                }
            }
        }
    }

    fun interface AdvancementEarn {
        fun onAdvancementEarn(player: ServerPlayerEntity, display: AdvancementDisplay)
    }

    fun interface PlayerJoin {
        fun onPlayerJoin(player: ServerPlayerEntity, hasPlayedBefore: Boolean)
    }

    fun interface PlayerLeave {
        fun onPlayerLeave(player: ServerPlayerEntity)
    }

    fun interface PlayerDeath {
        fun onPlayerDeath(player: ServerPlayerEntity, damageSource: DamageSource)
    }
}
