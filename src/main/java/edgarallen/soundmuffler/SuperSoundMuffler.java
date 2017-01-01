package edgarallen.soundmuffler;


import com.google.common.collect.EvictingQueue;
import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import edgarallen.soundmuffler.gui.GuiHandler;
import edgarallen.soundmuffler.network.ThePacketeer;
import edgarallen.soundmuffler.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;

@Mod(modid = SuperSoundMuffler.MOD_ID, name = SuperSoundMuffler.NAME, version = SuperSoundMuffler.VERSION)
public class SuperSoundMuffler {
    public static final String MOD_ID = "supersoundmuffler";
    public static final String NAME = "Super Sound Muffler";
    public static final String VERSION = "1.0.0.2";

    @Mod.Instance(MOD_ID)
    public static SuperSoundMuffler instance;

    public static final Logger log = LogManager.getLogger(NAME);

    @SidedProxy(clientSide = "edgarallen.soundmuffler.proxy.ClientProxy", serverSide = "edgarallen.soundmuffler.proxy.CommonProxy")
    public static CommonProxy proxy;

    public Queue<ResourceLocation> recentSounds = EvictingQueue.create(16);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();
        ThePacketeer.init();

        if(event.getSide() == Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.blockSoundMuffler.registerRecipes();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlaySound(PlaySoundEvent event) {
        WorldClient theWorld = Minecraft.getMinecraft().theWorld;
        if(theWorld != null) {
            ISound sound = event.getSound();
            int minX = MathHelper.floor_double(sound.getXPosF() - 8f);
            int maxX = MathHelper.ceiling_double_int(sound.getXPosF() + 8f);
            int minY = MathHelper.floor_double(sound.getYPosF() - 8f);
            int maxY = MathHelper.ceiling_double_int(sound.getYPosF() + 8f);
            int minZ = MathHelper.floor_double(sound.getZPosF() - 8f);
            int maxZ = MathHelper.ceiling_double_int(sound.getZPosF() + 8f);

            for(int x = minX; x < maxX; ++x) {
                for(int z = minZ; z < maxZ; ++z) {
                    for(int y = minY; y < maxY; ++y) {
                        TileEntity tileEntity = theWorld.getTileEntity(new BlockPos(x, y, z));
                        if(tileEntity != null && tileEntity instanceof TileEntitySoundMuffler) {
                            TileEntitySoundMuffler tileEntitySoundMuffler = (TileEntitySoundMuffler) tileEntity;
                            if(tileEntitySoundMuffler.shouldMuffleSound(sound.getSoundLocation())) {
                                event.setResultSound(null);
                                return;
                            }
                        }
                    }
                }
            }

            recentSounds.offer(sound.getSoundLocation());
        }
    }

    public static int ticksInGame = 0;
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if(gui == null || !gui.doesGuiPauseGame()) {
                ticksInGame++;
            }
        }
    }
}
