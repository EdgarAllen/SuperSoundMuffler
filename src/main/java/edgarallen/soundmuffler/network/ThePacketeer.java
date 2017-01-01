package edgarallen.soundmuffler.network;

import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.network.messages.MessageAddRemoveSound;
import edgarallen.soundmuffler.network.messages.MessageToggleWhiteListMode;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ThePacketeer {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(SuperSoundMuffler.MOD_ID);
    private static int ids = 0;

    public static void init() {
        ThePacketeer.INSTANCE.registerMessage(MessageAddRemoveSound.Handler.class, MessageAddRemoveSound.class, ids++, Side.SERVER);
        ThePacketeer.INSTANCE.registerMessage(MessageToggleWhiteListMode.Handler.class, MessageToggleWhiteListMode.class, ids++, Side.SERVER);
    }

}
