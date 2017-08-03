package edgarallen.soundmuffler.network.messages;

import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static edgarallen.soundmuffler.network.messages.MessageToggleWhiteList.Type.Bauble;

public class MessageToggleWhiteList implements IMessage {

    BlockPos pos;
    Type type;

    public MessageToggleWhiteList() { }

    public MessageToggleWhiteList(BlockPos pos, Type type) {
        this.pos = pos;
        this.type = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        type = buf.readBoolean() ? Type.Bauble : Type.TileEntity;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeBoolean(type == Bauble);
    }

    public static class Handler implements IMessageHandler<MessageToggleWhiteList, IMessage> {
        public Handler() { }

        @Override
        public IMessage onMessage(final MessageToggleWhiteList message, final MessageContext ctx) {
            IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
            if (thread.isCallingFromMinecraftThread()) {
                handle(message,ctx);
            } else {
                thread.addScheduledTask(() -> handle(message, ctx));
            }

            return null;
        }

        private void handle(MessageToggleWhiteList message, MessageContext ctx) {
            switch (message.type) {
                case Bauble:
                    handleBauble(message, ctx);
                    break;
                case TileEntity:
                    handleTileEntity(message, ctx);
                    break;
            }
        }

        private void handleBauble(MessageToggleWhiteList message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            if(player != null) {
                ItemStack stack = player.getHeldItemMainhand();
                if(!stack.isEmpty() && stack.getItem() == SuperSoundMuffler.itemSoundMufflerBauble) {
                    SuperSoundMuffler.itemSoundMufflerBauble.toggleWhiteList(stack);
                }
            }
        }

        private void handleTileEntity(MessageToggleWhiteList message, MessageContext ctx) {
            World world = ctx.getServerHandler().player.world;
            TileEntity te = world.getTileEntity(message.pos);

            if (te != null && te instanceof TileEntitySoundMuffler) {
                TileEntitySoundMuffler tileEntity = (TileEntitySoundMuffler) te;
                tileEntity.toggleWhiteListMode();
            }
        }
    }

    public enum Type {
        Bauble,
        TileEntity
    }
}
