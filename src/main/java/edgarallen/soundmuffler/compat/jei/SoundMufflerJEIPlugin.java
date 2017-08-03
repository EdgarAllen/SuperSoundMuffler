package edgarallen.soundmuffler.compat.jei;

import edgarallen.soundmuffler.SuperSoundMuffler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class SoundMufflerJEIPlugin implements IModPlugin {

    @Override
    public void register (IModRegistry registry) {
        Block block = SuperSoundMuffler.blockSoundMuffler;
        registry.addIngredientInfo(new ItemStack(SuperSoundMuffler.blockSoundMuffler, 1), ItemStack.class,"jei." + block.getUnlocalizedName());

        Item item = SuperSoundMuffler.itemSoundMufflerBauble;
        registry.addIngredientInfo(new ItemStack(item, 1), ItemStack.class,"jei." + item.getUnlocalizedName());
    }
}
