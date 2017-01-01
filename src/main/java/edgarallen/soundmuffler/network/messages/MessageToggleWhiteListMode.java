package edgarallen.soundmuffler.network.messages;

import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageToggleWhiteListMode implements IMessage {

    BlockPos pos;

    public  MessageToggleWhiteListMode() { }

    public MessageToggleWhiteListMode(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    public static class Handler implements IMessageHandler<MessageToggleWhiteListMode, IMessage> {
        public Handler() { }

        @Override
        public IMessage onMessage(final MessageToggleWhiteListMode message, final MessageContext ctx) {
            IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
            if (thread.isCallingFromMinecraftThread()) {
                handle(message,ctx);
            } else {
                thread.addScheduledTask(() -> handle(message, ctx));
            }

            return null;
        }

        private void handle(MessageToggleWhiteListMode message, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            TileEntity te = world.getTileEntity(message.pos);

            if (te != null && te instanceof TileEntitySoundMuffler) {
                TileEntitySoundMuffler tileEntity = (TileEntitySoundMuffler) te;
                tileEntity.toggleWhiteListMode();
            }
        }
    }
}
