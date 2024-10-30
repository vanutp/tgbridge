package dev.vanutp.tgbridge.fabric

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.advancement.AdvancementEntry
import net.minecraft.server.network.ServerPlayerEntity

object MinecraftEvents {

    val PLAYER_ADVANCEMENT: Event<AdvancementEvent> =
        EventFactory.createArrayBacked(
            AdvancementEvent::class.java,
            { callbacks ->
                AdvancementEvent { player, advancement, isDone ->
                    callbacks.forEach {
                        it.give(player, advancement, isDone)
                    }
                }
            }
        )

    fun interface AdvancementEvent {
        fun give(
            player: ServerPlayerEntity,
            advancement: AdvancementEntry,
            isDone: Boolean
        )
    }
}