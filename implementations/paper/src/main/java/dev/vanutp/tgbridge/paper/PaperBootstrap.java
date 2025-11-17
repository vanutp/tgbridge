package dev.vanutp.tgbridge.paper;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PaperBootstrap extends JavaPlugin {
    public PaperTelegramBridge tgbridge;
    public List<String> missingLibs;

    private void checkLibs() {
        missingLibs = new ArrayList<>();
        final var requiredLibs = Arrays.asList(
            Map.entry("kotlin-stdlib", "kotlin.Unit"),
            Map.entry("kotlinx-coroutines", "kotlinx.coroutines.GlobalScope"),
            Map.entry("kotlinx-serialization", "kotlinx.serialization.json.Json")
        );
        for (final var lib : requiredLibs) {
            try {
                Class.forName(lib.getValue());
            } catch (ClassNotFoundException e) {
                missingLibs.add(lib.getKey());
            }
        }
    }

    public PaperBootstrap() {
        checkLibs();
        if (missingLibs.isEmpty()) {
            tgbridge = new PaperTelegramBridge(this);
        }
    }

    @Override
    public void onEnable() {
        if (!missingLibs.isEmpty()) {
            final var missingLibsString = String.join(", ", missingLibs);
            getLogger().severe("================================");
            getLogger().severe("tgbridge requires Kotlin libraries to run");
            getLogger().severe("Please install a Kotlin provider plugin, for example https://modrinth.com/plugin/kotlinmc");
            getLogger().severe("Missing libraries: " + missingLibsString);
            getLogger().severe("================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new EventManager(this).register();
        tgbridge.onEnable$tgbridge_paper();
    }

    @Override
    public void onDisable() {
        if (tgbridge != null) {
            tgbridge.shutdown();
        }
    }
}
