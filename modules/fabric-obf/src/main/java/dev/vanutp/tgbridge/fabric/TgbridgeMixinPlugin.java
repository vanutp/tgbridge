package dev.vanutp.tgbridge.fabric;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import net.minecraft.DetectedVersion;
import net.minecraft.util.GsonHelper;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TgbridgeMixinPlugin implements IMixinConfigPlugin {
    private static final String version;
    private static final int minor;
    private static final int patch;

    static {
        try (final var is = DetectedVersion.class.getResourceAsStream("/version.json")) {
            if (is == null) {
                throw new IllegalStateException("Failed to load Minecraft version");
            }
            try (final var reader = new InputStreamReader(is)) {
                version = GsonHelper.getAsString(GsonHelper.parse(reader), "name");
            }
        } catch (JsonParseException | IOException exception) {
            throw new IllegalStateException("Failed to load Minecraft version", exception);
        }

        final var splitVersion = version.split("\\.");
        minor = Integer.parseInt(splitVersion[1]);
        // TODO: can it be == 2?
        patch = splitVersion.length > 2 ? Integer.parseInt(splitVersion[2]) : 0;
    }

    private static final boolean isLte201 = minor < 20 || minor == 20 && patch <= 1;
    private static final boolean isLte215 = minor < 21 || minor == 21 && patch <= 5;
    private static final Map<String, Boolean> CONDITIONS = ImmutableMap.of(
        "PlayerListMixin_201", isLte201,
        "PlayerListMixin_modern", !isLte201,
        "PlayerAdvancementsMixin_201", isLte201,
        "PlayerAdvancementsMixin_modern", !isLte201,
        "ServerPlayerMixin_215", isLte215,
        "ServerPlayerMixin_modern", !isLte215
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        var className = mixinClassName.replace("dev.vanutp.tgbridge.fabric.mixin.", "");
        return CONDITIONS.getOrDefault(className, true);
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
