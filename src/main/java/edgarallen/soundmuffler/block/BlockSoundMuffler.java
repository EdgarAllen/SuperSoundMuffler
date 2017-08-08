package edgarallen.soundmuffler.block;

import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.gui.GuiHandler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockSoundMuffler extends BlockContainer {
    public static final String NAME = "sound_muffler";
    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);

    public BlockSoundMuffler() {
        super(Material.WOOD);
        setRegistryName(NAME);
        setUnlocalizedName(NAME);
        setHardness(0.1F);
        setResistance(10.0F);
        setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntitySoundMuffler && !te.isInvalid()) {
            playerIn.openGui(SuperSoundMuffler.instance, GuiHandler.SOUND_MUFFLER_GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }

        return false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof TileEntitySoundMuffler) {
            TileEntitySoundMuffler tileEntity = (TileEntitySoundMuffler) te;

            if (stack.hasTagCompound() && (stack.getTagCompound().hasKey("sounds"))) {
                tileEntity.readNBT(stack.getTagCompound());
            }
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }

    @Override
    public String getHarvestTool(@Nonnull IBlockState state) {
        return null;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<ItemStack> list = new ArrayList<>();
        ItemStack stack = new ItemStack(this, 1);
        TileEntity te = world.getTileEntity(pos);
        if(te != null && te instanceof TileEntitySoundMuffler) {
            TileEntitySoundMuffler tileEntity = (TileEntitySoundMuffler) te;
            if(!tileEntity.isDefault()) {
                NBTTagCompound compound = tileEntity.writeNBT(new NBTTagCompound());
                stack.setTagCompound(compound);
            }
        }
        list.add(stack);
        return list;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySoundMuffler();
    }

    @Override
    public int getMetaFromState (IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta (int meta) {
        return getDefaultState();
    }

    @Override
    public boolean isOpaqueCube (IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube (IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Override
    public boolean canRenderInLayer (IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.SOLID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
        if(stack.hasTagCompound()) {
            NBTTagCompound compound = stack.getTagCompound();

            boolean showWhiteListTooltip = !compound.hasKey("whiteList") || compound.getBoolean("whiteList");
            String key = showWhiteListTooltip ? "item.sound_muffler.tooltip.mode.white_list" : "item.sound_muffler.tooltip.mode.black_list";
            tooltip.add(I18n.format(key));

            int rangeIndex = compound.hasKey("rangeIndex") ? compound.getInteger("rangeIndex") : TileEntitySoundMuffler.getDefaultRangeIndex();
            tooltip.add(I18n.format("item.sound_muffler.tooltip.range", TileEntitySoundMuffler.getRange(rangeIndex)));

            if(compound.hasKey("sounds")) {
                NBTTagList tagList = compound.getTagList("sounds", 10);
                int count = tagList.tagCount();
                tooltip.add(I18n.format("item.sound_muffler.tooltip.sounds.count", count));
                if(GuiScreen.isShiftKeyDown()) {
                    for(int i = 0; i < tagList.tagCount(); ++i) {
                        NBTTagCompound sound = tagList.getCompoundTagAt(i);
                        tooltip.add(I18n.format("item.sound_muffler.tooltip.sound", sound.getString("sound")));
                    }
                }
            } else {
                tooltip.add(I18n.format("item.sound_muffler.tooltip.sounds.count", 0));
            }
        } else {
            tooltip.add(I18n.format("item.sound_muffler.tooltip.mode.white_list"));
            tooltip.add(I18n.format("item.sound_muffler.tooltip.range", TileEntitySoundMuffler.getDefaultRange()));
            tooltip.add(I18n.format("item.sound_muffler.tooltip.sounds.count", 0));
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName().toString(), "inventory"));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySoundMuffler.class, new RenderTileSoundMuffler());
    }
}
