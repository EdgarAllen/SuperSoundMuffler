package edgarallen.soundmuffler.compat.jei;

import edgarallen.soundmuffler.SuperSoundMuffler;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class SoundMufflerJEIPlugin extends BlankModPlugin {

    @Override
    public void register (IModRegistry registry) {
        Block block = SuperSoundMuffler.proxy.blockSoundMuffler;
        registry.addDescription(new ItemStack(SuperSoundMuffler.proxy.blockSoundMuffler, 1), "jei." + block.getUnlocalizedName());

        Item item = SuperSoundMuffler.proxy.itemSoundMufflerBauble;
        registry.addDescription(new ItemStack(item, 1), "jei." + item.getUnlocalizedName());
    }
}
