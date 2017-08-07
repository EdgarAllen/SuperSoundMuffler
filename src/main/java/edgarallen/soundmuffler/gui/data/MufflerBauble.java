package edgarallen.soundmuffler.gui.data;

import edgarallen.soundmuffler.network.ThePacketeer;
import edgarallen.soundmuffler.network.messages.MessageAddRemoveSound;
import edgarallen.soundmuffler.network.messages.MessageToggleWhiteList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MufflerBauble implements IMufflerAccessor {
    private EntityPlayer player;

    public MufflerBauble(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public boolean isWhiteList() {
        ItemStack bauble = player.getHeldItemMainhand();
        if(bauble.hasTagCompound()) {
            NBTTagCompound compound = bauble.getTagCompound();
            if(compound.hasKey("whiteList")) {
                return compound.getBoolean("whiteList");
            }
        }

        return false;
    }

    @Override
    public List<ResourceLocation> getMuffledSounds() {
        List<ResourceLocation> sounds = new ArrayList<>();

        ItemStack bauble = player.getHeldItemMainhand();
        if(bauble.hasTagCompound()) {
            NBTTagCompound compound = bauble.getTagCompound();
            if(compound.hasKey("sounds")) {
                NBTTagList list = compound.getTagList("sounds", 10);
                for(int i = 0; i < list.tagCount(); ++i) {
                    NBTTagCompound c = list.getCompoundTagAt(i);
                    String s = c.getString("sound");
                    sounds.add(new ResourceLocation(s));
                }
            }
        }

        Collections.sort(sounds, Comparator.comparing(ResourceLocation::toString));
        return sounds;
    }

    @Override
    public void toggleWhiteList() {
        ThePacketeer.INSTANCE.sendToServer(new MessageToggleWhiteList(BlockPos.ORIGIN, MessageToggleWhiteList.Type.Bauble));
    }

    @Override
    public void muffleSound(ResourceLocation sound) {
        ThePacketeer.INSTANCE.sendToServer(new MessageAddRemoveSound(BlockPos.ORIGIN, sound, MessageAddRemoveSound.Type.Bauble, MessageAddRemoveSound.Action.Add));
    }

    @Override
    public void unmuffleSound(ResourceLocation sound) {
        ThePacketeer.INSTANCE.sendToServer(new MessageAddRemoveSound(BlockPos.ORIGIN, sound, MessageAddRemoveSound.Type.Bauble, MessageAddRemoveSound.Action.Remove));
    }
}
