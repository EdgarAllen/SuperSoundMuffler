package edgarallen.soundmuffler.proxy;

import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    private Set<TileEntitySoundMuffler> soundMufflers = Collections.newSetFromMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        SuperSoundMuffler.blockSoundMuffler.registerModels();
        SuperSoundMuffler.itemSoundMufflerBauble.registerModels();
    }

    @Override
    public void cacheMuffler(TileEntitySoundMuffler tileEntity) {
        soundMufflers.add(tileEntity);
    }

    @Override
    public void uncacheMuffler(TileEntitySoundMuffler tileEntity) {
        soundMufflers.remove(tileEntity);
    }

    @Override
    public void clearCache() {
        soundMufflers.clear();
    }

    @Override
    public Set<TileEntitySoundMuffler> getTileEntities() {
        return soundMufflers;
    }
}
