package edgarallen.soundmuffler.gui.data;

import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IMufflerAccessor {

    boolean isWhiteList();

    List<ResourceLocation> getMuffledSounds();

    void toggleWhiteList();

    void muffleSound(ResourceLocation sound);

    void unmuffleSound(ResourceLocation sound);

}
