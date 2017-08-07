package edgarallen.soundmuffler.gui;

import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.gui.data.IMufflerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class GuiSoundMuffler extends GuiContainer {

    private static final ResourceLocation guiTexture = new ResourceLocation(SuperSoundMuffler.MOD_ID, "textures/gui/sound_muffler.png");

    private final IMufflerAccessor muffler;

    private GuiShortWideButton modeButton;
    private GuiShortButton addSoundButton;
    private GuiShortButton removeSoundButton;
    private GuiSlider rangeSlider;
    private GuiSoundList soundList;

    public GuiSoundMuffler(IMufflerAccessor muffler) {
        super(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        });

        this.xSize = 256;
        this.ySize = 170;
        this.muffler = muffler;
    }

    @Override
    public void initGui() {
        super.initGui();
        String key = muffler.isWhiteList() ? "tile.sound_muffler.gui.button.mode.white_list" : "tile.sound_muffler.gui.button.mode.black_list";
        modeButton = new GuiShortWideButton(0, guiLeft + 159, guiTop + 5, I18n.format(key));
        buttonList.add(modeButton);

        addSoundButton = new GuiShortButton(1, guiLeft + 159, guiTop + 151, I18n.format("tile.sound_muffler.gui.button.add") );
        buttonList.add(addSoundButton);

        removeSoundButton = new GuiShortButton(2, guiLeft + 205, guiTop + 151, I18n.format("tile.sound_muffler.gui.button.remove"));
        removeSoundButton.enabled = false;
        buttonList.add(removeSoundButton);

        if(muffler.isRanged()) {
            rangeSlider = new GuiSlider(3, guiLeft + 7, guiTop + 151, 0f, 19f);
            buttonList.add(rangeSlider);
        }

        soundList = new GuiSoundList(240, 126, guiTop + 22, guiTop + 148, guiLeft + 8, 14);

        List<ResourceLocation> sounds = muffler.getMuffledSounds();
        sounds.sort(Comparator.comparing(ResourceLocation::toString));
        soundList.setSounds(sounds);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        removeSoundButton.enabled = soundList.hasSelectedElements();

        List<ResourceLocation> sounds = muffler.getMuffledSounds();
        sounds.sort(Comparator.comparing(ResourceLocation::toString));
        soundList.setSounds(sounds);

        String key = muffler.isWhiteList() ? "tile.sound_muffler.gui.button.mode.white_list" : "tile.sound_muffler.gui.button.mode.black_list";
        modeButton.displayString = I18n.format(key);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button.enabled) {
            if (button.id == modeButton.id) {
                muffler.toggleWhiteList();
                String key = muffler.isWhiteList() ? "tile.sound_muffler.gui.button.mode.white_list" : "tile.sound_muffler.gui.button.mode.black_list";
                modeButton.displayString = I18n.format(key);
            } else if (button.id == addSoundButton.id) {
                Set<ResourceLocation> unique = new HashSet<>(SuperSoundMuffler.instance.recentSounds);
                Minecraft.getMinecraft().displayGuiScreen(new GuiSoundMufflerAddSound(this, muffler, new ArrayList<>(unique)));
            } else if(button.id == removeSoundButton.id) {
                List<ResourceLocation> selectedSounds = soundList.getSelectedSounds();
                for(ResourceLocation sound : selectedSounds) {
                    if(sound != null) {
                        muffler.unmuffleSound(sound);
                    }
                }
                soundList.clearSelection();
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(SuperSoundMuffler.NAME, 8, 9, 0x404040);
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
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (visible) {
                RenderHelper.disableStandardItemLighting();
                mc.getTextureManager().bindTexture(guiTexture);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                boolean hover = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
                drawTexturedModalRect(x, y, 0, ySize + (enabled ? hover ? 14 : 0 : 28), width, height);

                int colour = 0xE0E0E0;
                if (!enabled) {
                    colour = 0xA0A0A0;
                } else if (hover) {
                    colour = 0xFFFFA0;
                }
                drawCenteredString(fontRenderer, displayString, x + width / 2, y + (height - 8) / 2, colour);
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
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (visible) {
                RenderHelper.disableStandardItemLighting();
                mc.getTextureManager().bindTexture(guiTexture);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                boolean hover = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
                drawTexturedModalRect(x, y, 44, ySize + (enabled ? hover ? 14 : 0 : 28), width, height);

                int colour = 0xE0E0E0;
                if (!enabled) {
                    colour = 0xA0A0A0;
                } else if (hover) {
                    colour = 0xFFFFA0;
                }

                drawCenteredString(fontRenderer, displayString, x + width / 2, y + (height - 8) / 2, colour);
                RenderHelper.enableStandardItemLighting();
            }
        }
    }

    private final class GuiSoundList extends GuiScrollingList {
        private List<ResourceLocation> sounds;
        private final int slotHeight;
        private List<Integer> selectedIndicies = new ArrayList<>();

        GuiSoundList(int width, int height, int top, int bottom, int left, int slotHeight) {
            super(Minecraft.getMinecraft(), width, height, top, bottom, left, slotHeight, width, height);
            this.slotHeight = slotHeight;
        }

        @Override
        protected int getSize() {
            return sounds.size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {
            if(isCtrlKeyDown()) {
                if(isSelected(index)) {
                    removeSelection(index);
                } else {
                    selectIndex(index);
                }
            } else if(isShiftKeyDown()) {
                clearSelection();
                int start = index > selectedIndex ? selectedIndex : index;
                int end = index > selectedIndex ? index : selectedIndex;
                selectRange(start, end);
            } else {
                clearSelection();
                selectIndex(index);
            }
        }

        @Override
        protected boolean isSelected(int index) {
            for(int i : selectedIndicies) {
                if(i == index) {
                    return true;
                }
            }
            return false;
        }

        void removeSelection(int index) {
            for(int i = 0; i < selectedIndicies.size(); i++) {
                if(selectedIndicies.get(i) == index) {
                    selectedIndicies.remove(i);
                    return;
                }
            }
        }

        void selectIndex(int index) {
            removeSelection(index);
            selectedIndicies.add(index);
            selectedIndex = index;
        }

        void clearSelection() {
            selectedIndicies.clear();
        }

        void selectRange(int start, int end) {
            for(int i = start; i <= end; i++) {
                selectedIndicies.add(i);
            }
            selectedIndex = end;
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
            fontRenderer.drawString(fontRenderer.trimStringToWidth(sound.toString(), listWidth - 10), left + 3 , top +  2, 0xCCCCCC);
        }

        void setSounds(List<ResourceLocation> sounds) {
            this.sounds = sounds;
        }

        boolean hasSelectedElements() { return selectedIndicies.size() > 0; }

        List<ResourceLocation> getSelectedSounds() {
            List<ResourceLocation> ret = new ArrayList<>();

            for(int i : selectedIndicies) {
                ret.add(sounds.get(i));
            }

            return ret;
        }
    }

    private final class GuiSlider extends GuiButton {

        private float sliderValue;
        public boolean dragging;
        private final float minValue;
        private final float maxValue;

        public GuiSlider(int buttonId, int x, int y) {
            this(buttonId, x, y, 0.0F, 1.0F);
        }

        public GuiSlider(int buttonId, int x, int y, float minValueIn, float maxValueIn) {
            super(buttonId, x, y, 128, 14, I18n.format("tile.sound_muffler.gui.slider.range", muffler.getRange()));
            minValue = minValueIn;
            maxValue = maxValueIn;
            sliderValue = normalizeValue((float)muffler.getRangeIndex());
        }

        @Override
        protected int getHoverState(boolean mouseOver) {
            return 0;
        }

        @Override
        protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
            if (visible) {
                if (dragging) {
                    sliderValue = (float)(mouseX - (x + 4)) / (float)(width - 8);
                    sliderValue = MathHelper.clamp(sliderValue, 0.0F, 1.0F);
                    float f = denormalizeValue(sliderValue);
                    muffler.setRange((int) f);
                    sliderValue = normalizeValue(f);
                    displayString = I18n.format("tile.sound_muffler.gui.slider.range", muffler.getRange());
                }

                mc.getTextureManager().bindTexture(guiTexture);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                drawTexturedModalRect(x + (int)(sliderValue * (float)(width - 8)), y, 128, 212, 8, 14);
            }
        }

        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (super.mousePressed(mc, mouseX, mouseY)) {
                sliderValue = (float)(mouseX - (x + 4)) / (float)(width - 8);
                sliderValue = MathHelper.clamp(sliderValue, 0.0F, 1.0F);
                muffler.setRange((int) denormalizeValue(sliderValue));
                displayString = I18n.format("tile.sound_muffler.gui.slider.range", muffler.getRange());
                dragging = true;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void mouseReleased(int mouseX, int mouseY) {
            dragging = false;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (visible) {
                RenderHelper.disableStandardItemLighting();
                mc.getTextureManager().bindTexture(guiTexture);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                drawTexturedModalRect(x, y, 0, 212, width, height);
                mouseDragged(mc, mouseX, mouseY);
                drawCenteredString(fontRenderer, displayString, x + width / 2, y + (height - 8) / 2, 0xE0E0E0);
                RenderHelper.enableStandardItemLighting();
            }
        }

        private float normalizeValue(float value) {
            return MathHelper.clamp(snapToStepClamp(value) / maxValue, 0.0F, 1.0F);
        }

        private float denormalizeValue(float value) {
            return snapToStepClamp(maxValue * MathHelper.clamp(value, 0.0F, 1.0F));
        }

        private float snapToStepClamp(float value) {
            value = snapToStep(value);
            return MathHelper.clamp(value, minValue, maxValue);
        }

        private float snapToStep(float value) {
            return Math.round(value / 1);
        }
    }
}
