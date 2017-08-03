package edgarallen.soundmuffler.proxy;

import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.bauble.ItemSoundMufflerBauble;
import edgarallen.soundmuffler.block.BlockSoundMuffler;
import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber
public class CommonProxy {
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockSoundMuffler());
        GameRegistry.registerTileEntity(TileEntitySoundMuffler.class, SuperSoundMuffler.MOD_ID + ":" + BlockSoundMuffler.NAME);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemSoundMufflerBauble());
        event.getRegistry().register(new ItemBlock(SuperSoundMuffler.blockSoundMuffler).setRegistryName(SuperSoundMuffler.blockSoundMuffler.getRegistryName()));
    }
}
