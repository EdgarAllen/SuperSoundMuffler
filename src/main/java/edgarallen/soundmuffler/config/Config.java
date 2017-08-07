package edgarallen.soundmuffler.config;

import edgarallen.soundmuffler.SuperSoundMuffler;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Config {

    public static int MAX_RANGE = 32;
    public static final int MIN_RANGE = 1;

    private static Configuration config;

    public static void readConfig(File configFile) {
        config = new Configuration(configFile);
        try {
            config.load();
            read();
        } catch (Exception e) {
            SuperSoundMuffler.log.error("Problem loading config file", e);
        } finally {
            if(config.hasChanged()) {
                config.save();
            }
        }
    }

    private static void read() {
        MAX_RANGE = config.getInt("Max Range", "General", 32, 1, 128, "Maximum effective range of the sound muffler. Setting this too large can cause lag.");
    }
}
