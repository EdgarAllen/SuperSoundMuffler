package edgarallen.soundmuffler.block;

import edgarallen.soundmuffler.SuperSoundMuffler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import java.util.Random;

public class RenderTileSoundMuffler extends TileEntitySpecialRenderer<TileEntitySoundMuffler> {
    private static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(SuperSoundMuffler.MOD_ID + ":" + BlockSoundMuffler.NAME, "inventory");

    /**
     * Code commandeered from Botania[https://github.com/Vazkii/Botania/blob/master/src/main/java/vazkii/botania/client/render/tile/RenderTileFloatingFlower.java]
     * for dat sexy sinusoidal motion
     */
    @Override
    public void render(TileEntitySoundMuffler tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tile != null) {
            if (!tile.getWorld().isBlockLoaded(tile.getPos(), false)) {
                return;
            }

            BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
            GlStateManager.pushMatrix();
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.translate(x, y, z);

            double worldTime = SuperSoundMuffler.ticksInGame + partialTicks;
            worldTime += new Random(tile.getPos().hashCode()).nextInt(1000);

            GlStateManager.translate(0.5F, 0, 0.5F);
            GlStateManager.rotate(-((float) worldTime * 0.5F), 0F, 1F, 0F);
            GlStateManager.translate(-0.5F, (float) Math.sin(worldTime * 0.05F) * 0.1F, 0.5);

            GlStateManager.rotate(4F * (float) Math.sin(worldTime * 0.04F), 1F, 0F, 0F);

            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            IBlockState state = tile.getWorld().getBlockState(tile.getPos());
            state = state.getBlock().getExtendedState(state, tile.getWorld(), tile.getPos());
            IBakedModel model = brd.getBlockModelShapes().getModelManager().getModel(MODEL_LOCATION);
            brd.getBlockModelRenderer().renderModelBrightness(model, state, 1.0F, true);

            GlStateManager.popMatrix();
        }
    }
}
