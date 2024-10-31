package dev.vanutp.tgbridge.fabric.mixin;

import dev.vanutp.tgbridge.fabric.CustomEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;

@Mixin(PlayerAdvancementTracker.class)
abstract class PlayerAdvancementTrackerMixin_201 {
    @Shadow
    private ServerPlayerEntity owner;

    @Dynamic
    @Inject(
        method = "method_12878",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            shift = At.Shift.AFTER
        )
    )
    private void grantCriterion(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final var display = (AdvancementDisplay) Advancement.class.getMethod("method_686").invoke(advancement);
        if (display == null || !display.shouldAnnounceToChat()) {
            return;
        }
        final var frame = display.getFrame();
        if (frame == null) {
            return;
        }
        final var type = frame.name().toLowerCase();

        final var toHoverableText = Advancement.class.getMethod("method_684");
        final var name = (Text) toHoverableText.invoke(advancement);
        CustomEvents.Companion.getADVANCEMENT_EARN_EVENT().invoker().onAdvancementEarn(owner, type, name);
    }
}
