package edgarallen.soundmuffler.network.messages;

import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAddRemoveSound implements IMessage {

    private BlockPos pos;
    private ResourceLocation sound;
    private Type type;
    private Action action;

    public MessageAddRemoveSound() { }

    public MessageAddRemoveSound(BlockPos pos, ResourceLocation sound, Type type, Action action) {
        this.pos = pos;
        this.sound = sound;
        this.type = type;
        this.action = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        sound = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        type = buf.readBoolean() ? Type.Bauble : Type.TileEntity;
        action = buf.readBoolean() ? Action.Add : Action.Remove;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        ByteBufUtils.writeUTF8String(buf, sound.toString());
        buf.writeBoolean(type == Type.Bauble);
        buf.writeBoolean(action == Action.Add);
    }

    public static class Handler implements IMessageHandler<MessageAddRemoveSound, IMessage> {
        public Handler() { }

        @Override
        public IMessage onMessage(final MessageAddRemoveSound message, final MessageContext ctx) {
            IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
            if (thread.isCallingFromMinecraftThread()) {
                handle(message,ctx);
            } else {
                thread.addScheduledTask(() -> handle(message, ctx));
            }

            return null;
        }

        private void handle(MessageAddRemoveSound message, MessageContext ctx) {
            switch (message.type) {
                case Bauble:
                    handleBauble(message, ctx);
                    break;
                case TileEntity:
                    handleTileEntity(message, ctx);
                    break;
            }
        }

        private void handleBauble(MessageAddRemoveSound message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            if (player != null) {
                ItemStack stack = player.getHeldItemMainhand();
                if(!stack.isEmpty() && stack.getItem() == SuperSoundMuffler.itemSoundMufflerBauble) {
                    if(message.action == Action.Add) {
                        SuperSoundMuffler.itemSoundMufflerBauble.muffleSound(stack, message.sound);
                    } else {
                        SuperSoundMuffler.itemSoundMufflerBauble.unmuffleSound(stack, message.sound);
                    }
                }
            }
        }

        private void handleTileEntity(MessageAddRemoveSound message, MessageContext ctx) {
            World world = ctx.getServerHandler().player.world;
            TileEntity te = world.getTileEntity(message.pos);
            if (te != null && te instanceof TileEntitySoundMuffler) {
                TileEntitySoundMuffler tileEntity = (TileEntitySoundMuffler) te;
                if(message.action == Action.Add) {
                    tileEntity.muffleSound(message.sound);
                } else {
                    tileEntity.unmuffleSound(message.sound);
                }
            }
        }
    }

    public enum Action {
        Add,
        Remove
    }

    public enum Type {
        Bauble,
        TileEntity
    }
}