package edgarallen.soundmuffler.proxy;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();

        blockSoundMuffler.registerModels();
        itemSoundMufflerBauble.registerModels();
    }
}
