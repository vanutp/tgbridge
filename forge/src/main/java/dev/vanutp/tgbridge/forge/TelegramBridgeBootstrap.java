package dev.vanutp.tgbridge.forge;

import net.minecraftforge.fml.common.Mod;

@Mod(TelegramBridge.MOD_ID)
public final class TelegramBridgeBootstrap {
    private final TelegramBridge mod = new TelegramBridge();
    public TelegramBridgeBootstrap() {
        System.out.println("meow meow from forge");
        mod.init();
    }
}
