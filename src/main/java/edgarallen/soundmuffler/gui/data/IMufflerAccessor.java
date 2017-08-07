package edgarallen.soundmuffler.gui.data;

import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IMufflerAccessor {

    boolean isWhiteList();

    List<ResourceLocation> getMuffledSounds();

    void toggleWhiteList();

    void muffleSound(ResourceLocation sound);

    void unmuffleSound(ResourceLocation sound);

    default boolean isRanged() { return false; }

    default int getRange() { return 0; }

    default int getRangeIndex() { return 0; }

    default void setRange(int value) { }
}
