package dev.vanutp.tgbridge.fabric.mixin.image;

import dev.vanutp.tgbridge.fabric.image.LoadedImage;
import dev.vanutp.tgbridge.fabric.image.PositionedImage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Style;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.client.font.TextRenderer$Drawer")
abstract class TextRendererMixin {
    @Shadow
    private float x;
    @Shadow
    @Final
    private Matrix4f matrix;
    @Shadow
    private float y;
    @Shadow
    @Final
    private VertexConsumerProvider vertexConsumers;
    @Unique
    private List<PositionedImage> images;


    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    void accept(int i, Style style, int j, CallbackInfoReturnable<Boolean> cir) {
        if (style == null || style.getHoverEvent() == null) {
            return;
        }
//        var imageContent = style.getHoverEvent().getValue(TextImageContent.Companion.getSHOW_IMAGE_INLINE());
//        if (imageContent == null) {
//            return;
//        }
        var img = LoadedImage.Companion.load("123", "/home/fox/.var/app/org.unmojang.FjordLauncher/data/FjordLauncher/instances/Fabulously Optimized(9)/.minecraft/michiru.png");
        var posImg = new PositionedImage(x, img);
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(posImg);
        x += img.getWidth();
        cir.setReturnValue(true);
    }

    @Inject(method = "drawLayer", at = @At("RETURN"))
    void drawLayer(float x, CallbackInfoReturnable<Float> cir) {
        if (images == null) {
            return;
        }
        for (var img : images) {
            // DrawContext isn't available here, so drawing manually
            var renderLayer = RenderLayer.getGuiTextured(img.getImg().getTextureId());
            var vertexConsumer = vertexConsumers.getBuffer(renderLayer);
            var x1 = img.getX();
            var x2 = x1 + img.getImg().getWidth();
            var y1 = y;
            var y2 = y1 + img.getImg().getHeight();
            vertexConsumer.vertex(matrix, x1, y1, 0.0F).texture(0, 0).color(-1);
            vertexConsumer.vertex(matrix, x1, y2, 0.0F).texture(0, 1).color(-1);
            vertexConsumer.vertex(matrix, x2, y2, 0.0F).texture(1, 1).color(-1);
            vertexConsumer.vertex(matrix, x2, y1, 0.0F).texture(1, 0).color(-1);
        }
    }
}
