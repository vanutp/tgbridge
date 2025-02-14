package dev.vanutp.tgbridge.fabric.mixin.image;

import dev.vanutp.tgbridge.fabric.FabricTelegramBridge;
import dev.vanutp.tgbridge.fabric.image.HoverImageContent;
import dev.vanutp.tgbridge.fabric.image.LoadedImage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

@Mixin(DrawContext.class)
abstract class DrawContextMixin {
    @Shadow
    public abstract int getScaledWindowWidth();

    @Shadow
    public abstract int getScaledWindowHeight();

    @Shadow
    @Final
    private MatrixStack matrices;

    @Shadow
    public abstract void drawTexture(
        Function<Identifier, RenderLayer> renderLayers,
        Identifier sprite,
        int x,
        int y,
        float u,
        float v,
        int width,
        int height,
        int textureWidth,
        int textureHeight
    );

    @Inject(method = "drawHoverEvent", at = @At("HEAD"), cancellable = true)
    void drawHoverEvent(TextRenderer textRenderer, Style style, int mouseX, int mouseY, CallbackInfo ci) {
        if (style == null || style.getHoverEvent() == null) {
            return;
        }
//        var imageContent = style.getHoverEvent().getValue(HoverImageContent.Companion.getSHOW_IMAGE());
//        if (imageContent == null) {
//            return;
//        }
//        var id = imageContent.getId();
        var img = LoadedImage.Companion.load("123", "/home/fox/.var/app/org.unmojang.FjordLauncher/data/FjordLauncher/instances/Fabulously Optimized(9)/.minecraft/michiru.png");
        var w = img.getWidth();
        var h = img.getHeight();
        var screenWidth = getScaledWindowWidth();
        var screenHeight = getScaledWindowHeight();
        var x = Math.min(mouseX + 9, screenWidth - w - 3);
        var y = Math.min(mouseY + 9, screenHeight - h - 3);
        System.out.println(x + " " + y + " " + w + " " + h);
        matrices.push();
        TooltipBackgroundRenderer.render((DrawContext) (Object) this, x, y, w, h, 400, null);
        matrices.translate(0, 0, 400);
        drawTexture(RenderLayer::getGuiTextured, img.getTextureId(), x, y, 0, 0, w, h, w, h);
        matrices.pop();

        ci.cancel();
    }
}
