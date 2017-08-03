package edgarallen.soundmuffler.bauble;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.gui.GuiHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Optional.Interface(modid = "baubles", iface = "baubles.api.IBauble")
public class ItemSoundMufflerBauble extends Item implements IBauble {
    public static final String NAME = "sound_muffler_bauble";

    public ItemSoundMufflerBauble() {
        setUnlocalizedName(NAME);
        setRegistryName(NAME);
        setNoRepair();
        setMaxDamage(0);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);

        addPropertyOverride(new ResourceLocation("disabled"), new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            public float apply(@Nonnull ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                return isDisabled(stack) ? 1.0f : 0.0F;
            }
        });
    }

    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName().toString(), "inventory"));
    }

    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType (ItemStack itemstack) {
        return BaubleType.TRINKET;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return false;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand hand) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if(playerIn.isSneaking()) {
            toggleDisabled(playerIn, stack);
        } else {
            playerIn.openGui(SuperSoundMuffler.instance, GuiHandler.SOUND_MUFFLER_BAUBLE_GUI_ID, worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item.sound_muffler_bauble.tooltip.header"));

        if(stack.hasTagCompound()) {
            NBTTagCompound compound = stack.getTagCompound();

            boolean showWhiteListTooltip = !compound.hasKey("whiteList") || compound.getBoolean("whiteList");
            String key = showWhiteListTooltip ? "item.sound_muffler.tooltip.mode.white_list" : "item.sound_muffler.tooltip.mode.black_list";
            tooltip.add(I18n.format(key));

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
            tooltip.add(I18n.format("item.sound_muffler.tooltip.mode.black_list"));
            tooltip.add(I18n.format("item.sound_muffler.tooltip.sounds.count", 0));
        }
    }

    public boolean shouldMuffleSound(ItemStack stack, ResourceLocation sound) {
        if(!stack.hasTagCompound()) { return false; }

        NBTTagCompound compound = stack.getTagCompound();
        if(compound.hasKey("disabled")) { return false; }

        boolean isWhiteList = compound.hasKey("whiteList") && compound.getBoolean("whiteList");
        if(compound.hasKey("sounds")) {
            NBTTagList tags = compound.getTagList("sounds", 10);
            if(containsSound(tags, sound)) {
                return !isWhiteList;
            }
        }

        return isWhiteList;
    }

    public void toggleWhiteList(ItemStack stack) {
        boolean isWhiteList = false;

        if(stack.hasTagCompound()) {
            NBTTagCompound compound = stack.getTagCompound();
            if(compound.hasKey("whiteList")) {
                isWhiteList = compound.getBoolean("whiteList");
            }

            compound.setBoolean("whiteList", !isWhiteList);
            stack.setTagCompound(compound);
        } else {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setBoolean("whiteList", !isWhiteList);
            stack.setTagCompound(compound);
        }

    }

    public void muffleSound(ItemStack stack, ResourceLocation sound) {
        NBTTagCompound compound = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        NBTTagList tags = compound.hasKey("sounds") ? compound.getTagList("sounds", 10) : new NBTTagList();

        if(containsSound(tags, sound)) {
            return;
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("sound", sound.toString());
        tags.appendTag(tag);
        compound.setTag("sounds", tags);
        stack.setTagCompound(compound);
    }

    public void unmuffleSound(ItemStack stack, ResourceLocation sound) {
        if(stack.hasTagCompound()) {
            NBTTagCompound compound = stack.getTagCompound();
            if (compound.hasKey("sounds")) {
                NBTTagList tags = compound.getTagList("sounds", 10);
                NBTTagList newTags = new NBTTagList();
                for(int i = 0; i < tags.tagCount(); ++i) {
                    NBTTagCompound s = tags.getCompoundTagAt(i);
                    String soundLocation = s.getString("sound");
                    if(!soundLocation.equals(sound.toString())) {
                        newTags.appendTag(s);
                    }
                }
                compound.setTag("sounds", newTags);
                stack.setTagCompound(compound);
            }
        }
    }

    private boolean containsSound(NBTTagList tags, ResourceLocation sound) {
        for(int i = 0; i < tags.tagCount(); ++i) {
            NBTTagCompound s = tags.getCompoundTagAt(i);
            String soundLocation = s.getString("sound");
            if(soundLocation.equals(sound.toString())) {
                return true;
            }
        }

        return false;
    }

    private boolean isDisabled(ItemStack stack) {
        if(stack.hasTagCompound()) {
            NBTTagCompound compound = stack.getTagCompound();
            return compound.hasKey("disabled");
        }

        return false;
    }

    private void toggleDisabled(EntityPlayer playerIn, ItemStack stack) {
        if(stack.hasTagCompound()) {
            NBTTagCompound compound = stack.getTagCompound();
            if(compound.hasKey("disabled")) {
                compound.removeTag("disabled");
                stack.setTagCompound(compound);
                playerIn.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1F, 1F);
            } else {
                compound.setBoolean("disabled", true);
                stack.setTagCompound(compound);
                playerIn.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1F, 0.8F);
            }
        } else {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setBoolean("disabled", true);
            stack.setTagCompound(compound);
            playerIn.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1F, 0.8F);
        }
    }
}
