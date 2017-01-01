package edgarallen.soundmuffler.block.gui;

import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.block.TileEntitySoundMuffler;
import edgarallen.soundmuffler.network.ThePacketeer;
import edgarallen.soundmuffler.network.messages.MessageAddRemoveSound;
import edgarallen.soundmuffler.network.messages.MessageToggleWhiteListMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class GuiSoundMuffler extends GuiContainer {

    private static final ResourceLocation guiTexture = new ResourceLocation(SuperSoundMuffler.MOD_ID, "textures/gui/sound_muffler.png");

    private final TileEntitySoundMuffler tileEntity;

    private GuiShortWideButton modeButton;
    private GuiShortButton addSoundButton;
    private GuiShortButton removeSoundButton;
    private GuiSoundList soundList;

    public GuiSoundMuffler(TileEntitySoundMuffler tile) {
        super(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        });

        this.xSize = 256;
        this.ySize = 170;
        this.tileEntity = tile;
    }

    @Override
    public void initGui() {
        super.initGui();
        String key = tileEntity.isWhiteList() ? "tile.sound_muffler.gui.button.mode.white_list" : "tile.sound_muffler.gui.button.mode.black_list";
        modeButton = new GuiShortWideButton(0, guiLeft + 159, guiTop + 5, I18n.format(key));
        buttonList.add(modeButton);

        addSoundButton = new GuiShortButton(1, guiLeft + 7, guiTop + 151, I18n.format("tile.sound_muffler.gui.button.add") );
        buttonList.add(addSoundButton);

        removeSoundButton = new GuiShortButton(2, guiLeft + 205, guiTop + 151, I18n.format("tile.sound_muffler.gui.button.remove"));
        removeSoundButton.enabled = false;
        buttonList.add(removeSoundButton);

        soundList = new GuiSoundList(240, 126, guiTop + 22, guiTop + 148, guiLeft + 8, 14);

        List<ResourceLocation> sounds = tileEntity.getMuffledSounds();
        Collections.sort(sounds, (soundA, soundsB) -> soundA.toString().compareTo(soundsB.toString()));
        soundList.setSounds(sounds);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        removeSoundButton.enabled = soundList.getSelectedIndex() >= 0;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button.enabled) {
            if (button.id == modeButton.id) {
                ThePacketeer.INSTANCE.sendToServer(new MessageToggleWhiteListMode(tileEntity.getPos()));
                tileEntity.toggleWhiteListMode();
                String key = tileEntity.isWhiteList() ? "tile.sound_muffler.gui.button.mode.white_list" : "tile.sound_muffler.gui.button.mode.black_list";
                modeButton.displayString = I18n.format(key);
            } else if (button.id == addSoundButton.id) {
                Set<ResourceLocation> unique = new HashSet<>(SuperSoundMuffler.instance.recentSounds);
                Minecraft.getMinecraft().displayGuiScreen(new GuiSoundMufflerAddSound(this, tileEntity, new ArrayList<>(unique)));
            } else if(button.id == removeSoundButton.id) {
                ResourceLocation sound = soundList.getSounds().remove(soundList.getSelectedIndex());
                soundList.selectIndex(-1);
                tileEntity.unmuffleSound(sound);
                ThePacketeer.INSTANCE.sendToServer(new MessageAddRemoveSound(tileEntity.getPos(), sound, MessageAddRemoveSound.Action.Remove));
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(SuperSoundMuffler.NAME, 8, 9, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderHelper.disableStandardItemLighting();
        int xPos = (width - xSize) / 2;
        int yPos = (height - ySize) / 2;

        mc.getTextureManager().bindTexture(guiTexture);
        drawTexturedModalRect(xPos, yPos, 0, 0, xSize, ySize);
        soundList.drawScreen(mouseX, mouseY, partialTicks);
        RenderHelper.enableStandardItemLighting();
    }

    private final class GuiShortButton extends GuiButton {
        GuiShortButton(int id, int xPos, int yPos, String label) {
            super(id, xPos, yPos, 44, 14, label);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (visible && tileEntity != null) {
                RenderHelper.disableStandardItemLighting();
                mc.getTextureManager().bindTexture(guiTexture);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                boolean hover = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
                drawTexturedModalRect(xPosition, yPosition, 0, ySize + (enabled ? hover ? 14 : 0 : 28), width, height);

                int colour = 0xE0E0E0;
                if (!enabled) {
                    colour = 0xA0A0A0;
                } else if (hover) {
                    colour = 0xFFFFA0;
                }
                drawCenteredString(fontRendererObj, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, colour);
                RenderHelper.enableStandardItemLighting();
            }
        }
    }

    private final class GuiShortWideButton extends GuiButton {
        GuiShortWideButton(int id, int xPos, int yPos, String label) {
            super(id, xPos, yPos, 90, 14, label);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (visible && tileEntity != null) {
                RenderHelper.disableStandardItemLighting();
                mc.getTextureManager().bindTexture(guiTexture);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                boolean hover = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
                drawTexturedModalRect(xPosition, yPosition, 44, ySize + (enabled ? hover ? 14 : 0 : 28), width, height);

                int colour = 0xE0E0E0;
                if (!enabled) {
                    colour = 0xA0A0A0;
                } else if (hover) {
                    colour = 0xFFFFA0;
                }

                drawCenteredString(fontRendererObj, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, colour);
                RenderHelper.enableStandardItemLighting();
            }
        }
    }

    private final class GuiSoundList extends GuiScrollingList {
        private List<ResourceLocation> sounds;
        private final int slotHeight;

        GuiSoundList(int width, int height, int top, int bottom, int left, int slotHeight) {
            super(Minecraft.getMinecraft(), width, height, top, bottom, left, slotHeight, width, height);
            this.slotHeight = slotHeight;
        }

        @Override
        protected int getSize() {
            return sounds.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) { }

        @Override
        protected boolean isSelected(int index) {
            return index == selectedIndex;
        }

        @Override
        protected void drawBackground() { }

        @Override
        protected int getContentHeight() {
            return (getSize()) * slotHeight + 1;
        }

        @Override
        protected void drawSlot(int idx, int right, int top, int height, Tessellator tess) {
            ResourceLocation sound = sounds.get(idx);
            fontRendererObj.drawString(fontRendererObj.trimStringToWidth(sound.toString(), listWidth - 10), left + 3 , top +  2, 0xCCCCCC);
        }

        void setSounds(List<ResourceLocation> sounds) {
            this.sounds = sounds;
            selectedIndex = -1;
        }

        List<ResourceLocation> getSounds() { return sounds; }

        int getSelectedIndex() {
            return selectedIndex;
        }

        void selectIndex(int index) {
            selectedIndex = index;
        }
    }
}
