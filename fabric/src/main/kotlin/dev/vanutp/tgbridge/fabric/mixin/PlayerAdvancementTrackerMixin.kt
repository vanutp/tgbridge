package dev.vanutp.tgbridge.fabric.mixin

import dev.vanutp.tgbridge.fabric.MinecraftEvents
import net.minecraft.advancement.AdvancementEntry
import net.minecraft.advancement.AdvancementProgress
import net.minecraft.advancement.PlayerAdvancementTracker
import net.minecraft.server.network.ServerPlayerEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable


// Thx, Xujiayao
@Mixin(PlayerAdvancementTracker::class)
abstract class PlayerAdvancementTrackerMixin {

    @Shadow
    private val owner: ServerPlayerEntity? = null

    @Shadow
    abstract fun getProgress(advancement: AdvancementEntry?): AdvancementProgress?

    @Inject(method = ["grantCriterion(Lnet/minecraft/advancement/AdvancementEntry;Ljava/lang/String;)Z"], at = [At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V", shift = At.Shift.AFTER)])
    private fun grantCriterion(advancementHolder: AdvancementEntry, string: String, cir: CallbackInfoReturnable<Boolean>) {
        if (owner!=null) MinecraftEvents.PLAYER_ADVANCEMENT.invoker().give(owner, advancementHolder, getProgress(advancementHolder)?.isDone ==true)
    }

}