package edgarallen.soundmuffler.compat.theoneprobe;

import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nullable;
import java.util.List;

public class SoundMufflerTheOneProbeDataProvider {

    public static void register() {
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "edgarallen.soundmuffler.compat.theoneprobe.SoundMufflerTheOneProbeDataProvider$GetTheOneProbe");
    }

    public static class GetTheOneProbe implements com.google.common.base.Function<ITheOneProbe, Void> {
        public static ITheOneProbe probe;

        @Nullable
        @Override
        public Void apply(ITheOneProbe theOneProbe) {
            probe = theOneProbe;
            probe.registerProvider(new IProbeInfoProvider() {
                @Override
                public String getID() {
                    return SuperSoundMuffler.proxy.blockSoundMuffler.getUnlocalizedName();
                }

                @Override
                public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
                    TileEntity te = world.getTileEntity(data.getPos());
                    if(te instanceof TileEntitySoundMuffler) {
                        TileEntitySoundMuffler tileEntity = (TileEntitySoundMuffler) te;
                        String key = tileEntity.isWhiteList() ? "the_one_probe.sound_muffler.tooltip.mode.white_list" : "the_one_probe.sound_muffler.tooltip.mode.black_list";
                        probeInfo.horizontal().text(I18n.format(key));

                        List<ResourceLocation> sounds = tileEntity.getMuffledSounds();
                        if(sounds.isEmpty()) {
                            probeInfo.horizontal().text(I18n.format("the_one_probe.sound_muffler.tooltip.sounds.count", 0));
                        } else {
                            probeInfo.horizontal().text(I18n.format("the_one_probe.sound_muffler.tooltip.sounds.count", sounds.size()));
                            if (mode == ProbeMode.EXTENDED) {
                                IProbeInfo vertical = probeInfo.vertical(new LayoutStyle().borderColor(0xffff4444).spacing(2));

                                for (ResourceLocation sound : sounds) {
                                    vertical.text(I18n.format("the_one_probe.sound_muffler.tooltip.sound", sound.toString()));
                                }
                            }
                        }

                    }
                }
            });

            return null;
        }
    }
}
