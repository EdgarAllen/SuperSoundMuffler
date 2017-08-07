package edgarallen.soundmuffler.gui.data;

import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import edgarallen.soundmuffler.network.ThePacketeer;
import edgarallen.soundmuffler.network.messages.MessageAddRemoveSound;
import edgarallen.soundmuffler.network.messages.MessageSetRange;
import edgarallen.soundmuffler.network.messages.MessageToggleWhiteList;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class MufflerTileEntity implements IMufflerAccessor {
    private final TileEntitySoundMuffler tileEntity;

    public MufflerTileEntity(TileEntitySoundMuffler tileEntity) {
        this.tileEntity = tileEntity;
    }

    @Override
    public boolean isWhiteList() {
        return tileEntity.isWhiteList();
    }

    @Override
    public List<ResourceLocation> getMuffledSounds() {
        return tileEntity.getMuffledSounds();
    }

    @Override
    public void toggleWhiteList() {
        ThePacketeer.INSTANCE.sendToServer(new MessageToggleWhiteList(tileEntity.getPos(), MessageToggleWhiteList.Type.TileEntity));
    }

    @Override
    public void muffleSound(ResourceLocation sound) {
        ThePacketeer.INSTANCE.sendToServer(new MessageAddRemoveSound(tileEntity.getPos(), sound, MessageAddRemoveSound.Type.TileEntity, MessageAddRemoveSound.Action.Add));
    }

    @Override
    public void unmuffleSound(ResourceLocation sound) {
        ThePacketeer.INSTANCE.sendToServer(new MessageAddRemoveSound(tileEntity.getPos(), sound, MessageAddRemoveSound.Type.TileEntity, MessageAddRemoveSound.Action.Remove));
    }

    @Override
    public boolean isRanged() {
        return true;
    }

    @Override
    public int getRange() {
        return tileEntity.getRange();
    }

    @Override
    public int getRangeIndex() {
        return tileEntity.getRangeIndex();
    }

    @Override
    public void setRange(int value) {
        ThePacketeer.INSTANCE.sendToServer(new MessageSetRange(tileEntity.getPos(), value));
    }
}
