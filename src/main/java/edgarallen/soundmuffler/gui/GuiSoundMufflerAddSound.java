package edgarallen.soundmuffler.gui;

import edgarallen.soundmuffler.SuperSoundMuffler;
import edgarallen.soundmuffler.gui.data.IMufflerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSoundMufflerAddSound extends GuiContainer {
    private static final ResourceLocation guiTexture = new ResourceLocation(SuperSoundMuffler.MOD_ID, "textures/gui/sound_muffler_add_sound.png");

    private static final int KEYCODE_ENTER = 28;
    private static final int KEYCODE_KP_ENTER = 156;
    private static final int TEXT_COLOR_FOCUSED = 0xE0E0E0;
    private static final int TEXT_COLOR_ACTIVE = 0xAAAAAA;
    private static final int TEXT_COLOR_DISABLED = 0x404040;

    private final GuiScreen prevScreen;
    private final IMufflerAccessor muffler;

    private GuiShortButton allSoundsButton;
    private GuiShortButton recentSoundsButton;
    private GuiShortButton addSoundButton;
    private GuiShortButton cancelButton;
    private GuiTextField searchField;
    private GuiSoundList soundList;

    private String lastFilterText = "";
    private boolean showAllSounds = true;

    private final List<ResourceLocation> recentSounds;
    private List<ResourceLocation> allSounds;

    GuiSoundMufflerAddSound(GuiScreen prevScreen, IMufflerAccessor muffler, List<ResourceLocation> recentSounds) {
        super(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return false;
            }
        });

        this.xSize = 256;
        this.ySize = 170;
        this.prevScreen = prevScreen;
        this.muffler = muffler;
        this.recentSounds = recentSounds;
        lazyLoadAllSoundsList();
    }

    private void lazyLoadAllSoundsList() {
        allSounds = new ArrayList<>();
        allSounds.addAll(SoundEvent.REGISTRY.getKeys());
        Collections.sort(allSounds, (soundA, soundsB) -> soundA.toString().compareTo(soundsB.toString()));
    }

    @Override
    public void initGui() {
        super.initGui();

        allSoundsButton = new GuiShortButton(0, guiLeft + 159, guiTop + 5, I18n.format("tile.sound_muffler.add_sound.gui.button.all"));
        buttonList.add(allSoundsButton);
        allSoundsButton.enabled = false;
        recentSoundsButton = new GuiShortButton(1, guiLeft + 205, guiTop + 5, I18n.format("tile.sound_muffler.add_sound.gui.button.recent"));
        buttonList.add(recentSoundsButton);
        addSoundButton = new GuiShortButton(2, guiLeft + 159, guiTop + 151, I18n.format("tile.sound_muffler.add_sound.gui.button.add"));
        buttonList.add(addSoundButton);
        addSoundButton.enabled = false;
        cancelButton = new GuiShortButton(3, guiLeft + 205, guiTop + 151, I18n.format("tile.sound_muffler.add_sound.gui.button.cancel"));
        buttonList.add(cancelButton);

        soundList = new GuiSoundList(240, 112, guiTop + 22, guiTop + 134, guiLeft + 8, 14);
        soundList.setSounds(allSounds);

        Keyboard.enableRepeatEvents(true);
        searchField = new GuiTextField(2, fontRenderer, guiLeft + 11, guiTop + 139, 232, fontRenderer.FONT_HEIGHT);
        searchField.setMaxStringLength(256);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setTextColor(0xE0E0E0);
        searchField.setCanLoseFocus(true);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button.enabled) {
            if(button.id == recentSoundsButton.id) {
                recentSoundsButton.enabled = false;
                allSoundsButton.enabled = true;
                showAllSounds = false;
                updateSoundsList(recentSounds);
                return;
            }

            if(button.id == allSoundsButton.id) {
                allSoundsButton.enabled = false;
                recentSoundsButton.enabled = true;
                showAllSounds = true;
                updateSoundsList(allSounds);
                return;
            }

            if(button.id == addSoundButton.id) {
                ResourceLocation sound = soundList.getSounds().get(soundList.getSelectedIndex());
                if(sound != null) {
                    muffler.muffleSound(sound);
                }

                mc.displayGuiScreen(prevScreen);
                return;
            }

            if(button.id == cancelButton.id) {
                mc.displayGuiScreen(prevScreen);
                return;
            }
        }
        super.actionPerformed(button);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        searchField.updateCursorCounter();

        if (!searchField.getText().equals(lastFilterText)) {
            lastFilterText = searchField.getText();
            updateSoundsList(showAllSounds ? allSounds : recentSounds);
        }

        addSoundButton.enabled = soundList.getSelectedIndex() >= 0;
    }

    private void updateSoundsList(List<ResourceLocation> sounds) {
        if(lastFilterText.isEmpty()) {
            soundList.setSounds(sounds);
        } else {
            soundList.setSounds(sounds.stream().filter(sound -> sound.toString().toLowerCase().contains(lastFilterText.toLowerCase())).collect(Collectors.toList()));
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        searchField.mouseClicked(x, y, button);
        if (button == 1 && x >= searchField.x && x < searchField.x + searchField.width && y >= searchField.y && y < searchField.y + searchField.height) {
            searchField.setText("");
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();
        soundList.handleMouseInput(mouseX, mouseY);
    }

    @Override
    protected void keyTyped(char c, int keyCode) throws IOException {
        if(keyCode != KEYCODE_ENTER && keyCode != KEYCODE_KP_ENTER) {
            if(!searchField.textboxKeyTyped(c, keyCode)) {
                super.keyTyped(c, keyCode);
            }
        } else {
            if(searchField.isFocused()) {
                searchField.setFocused(false);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("tile.sound_muffler.add_sound.gui.title"), 8, 9, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderHelper.disableStandardItemLighting();
        int xPos = (width - xSize) / 2;
        int yPos = (height - ySize) / 2;

        mc.getTextureManager().bindTexture(guiTexture);
        drawTexturedModalRect(xPos, yPos, 0, 0, xSize, ySize);
        drawSearchField();
        soundList.drawScreen(mouseX, mouseY, partialTicks);
        RenderHelper.enableStandardItemLighting();
    }

    private void drawSearchField() {
        if(searchField.getText().isEmpty() && !searchField.isFocused()) {
            fontRenderer.drawString(I18n.format("tile.sound_muffler.add_sound.gui.search"), guiLeft + 11, guiTop + 139, TEXT_COLOR_DISABLED);
        } else {
            searchField.setTextColor(searchField.isFocused() ? TEXT_COLOR_FOCUSED : TEXT_COLOR_ACTIVE);
            searchField.drawTextBox();
        }
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
            fontRenderer.drawString(fontRenderer.trimStringToWidth(sound.toString(), listWidth - 10), left + 3 , top +  2, 0xCCCCCC);
        }

        void setSounds(List<ResourceLocation> sounds) {
            this.sounds = sounds;
            selectedIndex = -1;
        }

        int getSelectedIndex() { return selectedIndex; }

        List<ResourceLocation> getSounds() { return sounds; }
    }
}
