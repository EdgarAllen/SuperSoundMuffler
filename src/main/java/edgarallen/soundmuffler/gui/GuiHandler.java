package edgarallen.soundmuffler.gui;

import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import edgarallen.soundmuffler.gui.data.MufflerBauble;
import edgarallen.soundmuffler.gui.data.IMufflerAccessor;
import edgarallen.soundmuffler.gui.data.MufflerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
    public static final int SOUND_MUFFLER_GUI_ID = 0;
    public static final int SOUND_MUFFLER_BAUBLE_GUI_ID = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(ID == SOUND_MUFFLER_GUI_ID) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            if(tile instanceof TileEntitySoundMuffler) {
                IMufflerAccessor muffler = new MufflerTileEntity((TileEntitySoundMuffler)tile);
                return new GuiSoundMuffler(muffler);
            }
        } else if(ID == SOUND_MUFFLER_BAUBLE_GUI_ID) {
            ItemStack stack = player.getHeldItemMainhand();
            if(!stack.isEmpty() && stack.getItem() == SuperSoundMuffler.itemSoundMufflerBauble) {
                IMufflerAccessor muffler = new MufflerBauble(player);
                return new GuiSoundMuffler(muffler);
            }
        }

        return null;
    }
}
