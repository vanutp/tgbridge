package dev.vanutp.tgbridge.fabric.mixin;

import dev.vanutp.tgbridge.fabric.CustomEvents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
abstract class PlayerAdvancementTrackerMixin_modern {
    @Shadow
    private ServerPlayerEntity owner;

    @Inject(
        method = "grantCriterion",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            shift = At.Shift.AFTER
        )
    )
    private void grantCriterion(AdvancementEntry _advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        final var advancement = _advancement.value();
        final var display = advancement.display().orElse(null);
        if (display == null) {
            return;
        }
        CustomEvents.Companion.getADVANCEMENT_EARN_EVENT().invoker().onAdvancementEarn(owner, display);
    }
}
