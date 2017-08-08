package edgarallen.soundmuffler.compat.waila;

import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.util.List;
import java.util.stream.Collectors;

public class SoundMufflerWailaDataProvider implements IWailaDataProvider {
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntitySoundMuffler tileEntity = (TileEntitySoundMuffler) accessor.getTileEntity();
        String key = tileEntity.isWhiteList() ? "item.sound_muffler.tooltip.mode.white_list" : "item.sound_muffler.tooltip.mode.black_list";
        currenttip.add(I18n.format(key));
        currenttip.add(I18n.format("item.sound_muffler.tooltip.range", tileEntity.getRange()));
        List<ResourceLocation> sounds = tileEntity.getMuffledSounds();
        if(sounds.isEmpty()) {
            currenttip.add(I18n.format("item.sound_muffler.tooltip.sounds.count", 0));
        } else {
            currenttip.add(I18n.format("item.sound_muffler.tooltip.sounds.count", sounds.size()));
            if (GuiScreen.isShiftKeyDown()) {
                currenttip.addAll(sounds.stream().map(sound -> I18n.format("item.sound_muffler.tooltip.sound", sound.toString())).collect(Collectors.toList()));
            }
        }

        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        return null;
    }

    public static void register() {
        FMLInterModComms.sendMessage("waila", "register", "edgarallen.soundmuffler.compat.waila.SoundMufflerWailaDataProvider.callbackRegister");
    }

    public static void callbackRegister(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(new SoundMufflerWailaDataProvider(), TileEntitySoundMuffler.class);
    }
}
