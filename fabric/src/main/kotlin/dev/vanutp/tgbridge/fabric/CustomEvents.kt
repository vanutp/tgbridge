package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.fabric.CustomEvents.*
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text


class CustomEvents {
    companion object {
        val ADVANCEMENT_EARN_EVENT = EventFactory.createArrayBacked(AdvancementEarn::class.java) { handlers ->
            AdvancementEarn { player, advancementType, advancementNameComponent ->
                for (handler in handlers) {
                    handler.onAdvancementEarn(player, advancementType, advancementNameComponent)
                }
            }
        }
        val PLAYER_JOIN_EVENT = EventFactory.createArrayBacked(PlayerJoin::class.java) { handlers ->
            PlayerJoin { player ->
                for (handler in handlers) {
                    handler.onPlayerJoin(player)
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
        fun onAdvancementEarn(player: ServerPlayerEntity, advancementType: String, advancementNameComponent: Text): Unit
    }

    fun interface PlayerJoin {
        fun onPlayerJoin(player: ServerPlayerEntity): Unit
    }

    fun interface PlayerLeave {
        fun onPlayerLeave(player: ServerPlayerEntity): Unit
    }

    fun interface PlayerDeath {
        fun onPlayerDeath(player: ServerPlayerEntity, damageSource: DamageSource): Unit
    }
}
