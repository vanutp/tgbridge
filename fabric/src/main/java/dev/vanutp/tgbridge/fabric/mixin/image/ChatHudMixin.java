package dev.vanutp.tgbridge.fabric.mixin.image;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatHud.class)
abstract class ChatHudMixin {
    @Inject(method = "getTextStyleAt", at = @At("HEAD"))
    void getTextStyleAt(double x, double y, CallbackInfoReturnable<Style> cir) {
        // TODO: return correct values for image positions
    }
}
