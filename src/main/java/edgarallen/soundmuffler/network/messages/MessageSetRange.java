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

public class MessageSetRange implements IMessage {
    BlockPos pos;
    int rangeIndex;

    public MessageSetRange() { }

    public MessageSetRange(BlockPos pos, int value) {
        this.pos = pos;
        this.rangeIndex = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        rangeIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(rangeIndex);
    }

    public static class Handler implements IMessageHandler<MessageSetRange, IMessage> {
        public Handler() { }

        @Override
        public IMessage onMessage(final MessageSetRange message, final MessageContext ctx) {
            IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
            if (thread.isCallingFromMinecraftThread()) {
                handle(message,ctx);
            } else {
                thread.addScheduledTask(() -> handle(message, ctx));
            }

            return null;
        }

        private void handle(MessageSetRange message, MessageContext ctx) {
            World world = ctx.getServerHandler().player.world;
            TileEntity te = world.getTileEntity(message.pos);

            if (te != null && te instanceof TileEntitySoundMuffler) {
                TileEntitySoundMuffler tileEntity = (TileEntitySoundMuffler) te;
                tileEntity.setRange(message.rangeIndex);
            }
        }
    }
}
