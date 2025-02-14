package dev.vanutp.tgbridge.fabric.mixin.image;

import dev.vanutp.tgbridge.fabric.image.HoverImageContent;
import dev.vanutp.tgbridge.fabric.image.TextImageContent;
import net.minecraft.text.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(HoverEvent.Action.class)
abstract class HoverEventActionMixin {
    @Inject(method = "method_54192", at = @At("RETURN"), cancellable = true)
    private static void unvalidatedCodec_createBasicCodec(CallbackInfoReturnable<HoverEvent.Action[]> cir) {
        var existing = cir.getReturnValue();
        var lst = new ArrayList<>(Arrays.asList(existing));
        lst.add(HoverImageContent.Companion.getSHOW_IMAGE());
        lst.add(TextImageContent.Companion.getSHOW_IMAGE_INLINE());
        var res = lst.toArray(new HoverEvent.Action[0]);
        cir.setReturnValue(res);
    }
}
