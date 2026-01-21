package dev.vanutp.tgbridge.fabric.mixin;

import dev.vanutp.tgbridge.fabric.CustomEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
abstract class PlayerAdvancementsMixin_modern {
    @Shadow
    private ServerPlayer player;

    @Inject(
        method = "award",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V",
            shift = At.Shift.AFTER
        )
    )
    private void award(AdvancementHolder _advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        final var advancement = _advancement.value();
        final var display = advancement.display().orElse(null);
        if (display == null) {
            return;
        }
        CustomEvents.Companion.getADVANCEMENT_EARN_EVENT().invoker().onAdvancementEarn(player, display);
    }
}
