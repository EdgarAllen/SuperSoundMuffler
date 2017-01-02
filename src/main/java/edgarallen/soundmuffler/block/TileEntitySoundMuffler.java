package edgarallen.soundmuffler.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TileEntitySoundMuffler extends TileEntity {
    private HashSet<ResourceLocation> muffledSounds = new HashSet<>();
    private boolean whiteListMode = true;

    //region NBT Serialization
    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound = writeNBT(compound);
        return compound;
    }

    public NBTTagCompound writeNBT(NBTTagCompound compound) {
        NBTTagList tagList = new NBTTagList();
        for (ResourceLocation sound : muffledSounds) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("sound", sound.toString());
            tagList.appendTag(tag);
        }
        compound.setTag("sounds", tagList);
        compound.setBoolean("whiteList", whiteListMode);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readNBT(compound);
    }

    public void readNBT(NBTTagCompound compound) {
        muffledSounds.clear();
        whiteListMode = true;

        NBTTagList tagList = compound.getTagList("sounds", 10);
        for(int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound sound = tagList.getCompoundTagAt(i);
            muffledSounds.add(new ResourceLocation(sound.getString("sound")));
        }

        whiteListMode = compound.getBoolean("whiteList");
    }

    @Override
    @Nonnull
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = super.getUpdateTag();
        compound = writeNBT(compound);
        return compound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = getUpdateTag();
        return new SPacketUpdateTileEntity(pos, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    boolean isDefault() {
        return (whiteListMode && muffledSounds.isEmpty());

    }
    //endregion

    public void muffleSound(ResourceLocation sound) {
        muffledSounds.add(sound);

        markDirty();
        IBlockState state = worldObj.getBlockState(pos);
        worldObj.notifyBlockUpdate(pos, state, state, 4);
    }

    public void unmuffleSound(ResourceLocation sound) {
        muffledSounds.remove(sound);

        markDirty();
        IBlockState state = worldObj.getBlockState(pos);
        worldObj.notifyBlockUpdate(pos, state, state, 4);
    }

    public List<ResourceLocation> getMuffledSounds() {
        return new ArrayList<>(muffledSounds);
    }

    public void toggleWhiteListMode() {
        whiteListMode = !whiteListMode;

        markDirty();
        IBlockState state = worldObj.getBlockState(pos);
        worldObj.notifyBlockUpdate(pos, state, state, 4);
    }

    public boolean isWhiteList() { return whiteListMode; }

    public boolean shouldMuffleSound(ResourceLocation soundLocation) {
        if(isWhiteList()) {
            return !muffledSounds.contains(soundLocation);
        }

        return muffledSounds.contains(soundLocation);
    }
}
