package dev.vanutp.tgbridge.fabric.mixin;

import dev.vanutp.tgbridge.fabric.CustomEvents;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;

@Mixin(PlayerAdvancements.class)
abstract class PlayerAdvancementsMixin_201 {
    @Shadow
    private ServerPlayer player;

    @Dynamic
    @Inject(
        method = "method_12878",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V",
            shift = At.Shift.AFTER
        )
    )
    private void award(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final var display = (DisplayInfo) Advancement.class.getMethod("method_686").invoke(advancement);
        if (display == null) {
            return;
        }
        CustomEvents.Companion.getADVANCEMENT_EARN_EVENT().invoker().onAdvancementEarn(player, display);
    }
}
