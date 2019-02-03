package edgarallen.soundmuffler.compat.top;

import com.google.common.base.Function;
import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class TOPCompatibility {

    private static boolean registered;

    public static void register() {
        if (registered)
            return;
        registered = true;
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "edgarallen.soundmuffler.compat.top.TOPCompatibility$GetTheOneProbe");
    }

    public static class GetTheOneProbe implements Function<ITheOneProbe, Void> {

        public static ITheOneProbe probe;

        @Nullable
        @Override
        public Void apply(ITheOneProbe theOneProbe) {
            probe = theOneProbe;
            probe.registerProvider(new IProbeInfoProvider() {
                @Override
                public String getID() {
                    return "supersoundmuffler:default";
                }

                @Override
                public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
                    TileEntity te = world.getTileEntity(data.getPos());
                    if (te instanceof TileEntitySoundMuffler) {
                        TileEntitySoundMuffler tileEntity = (TileEntitySoundMuffler) te;
                        String key = tileEntity.isWhiteList() ? "item.sound_muffler.tooltip.mode.white_list" : "item.sound_muffler.tooltip.mode.black_list";
                        probeInfo.horizontal().text(I18n.format(key));
                        probeInfo.horizontal().text(I18n.format("item.sound_muffler.tooltip.range", tileEntity.getRange()));
                        List<ResourceLocation> sounds = tileEntity.getMuffledSounds();
                        if(sounds.isEmpty()) {
                            probeInfo.horizontal().text(I18n.format("item.sound_muffler.tooltip.sounds.count", 0));
                        } else {
                            probeInfo.horizontal().text(I18n.format("item.sound_muffler.tooltip.sounds.count", sounds.size()));
                            if (GuiScreen.isShiftKeyDown()) {
                                sounds.stream().map(sound -> I18n.format("item.sound_muffler.tooltip.sound", sound.toString()))
                                        .collect(Collectors.toList())
                                        .forEach(item->probeInfo.horizontal().text(item));

                            }
                        }

                    }
                }
            });

            return null;
        }
    }
}
