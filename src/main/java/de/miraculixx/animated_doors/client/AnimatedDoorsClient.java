package de.miraculixx.animated_doors.client;

import com.mojang.logging.LogUtils;
import de.miraculixx.animated_doors.client.config.AnimatedDoorsConfig;
import org.slf4j.Logger;

import java.nio.file.Path;

public final class AnimatedDoorsClient {
    public static final String MOD_ID = "animated_doors";
    private static final Logger LOGGER = LogUtils.getLogger();

    private AnimatedDoorsClient() {
    }

    public static void init(String loaderName, String version, Path configDir) {
        AnimatedDoorsConfig.instance().configure(configDir);
        AnimatedDoorsConfig.instance().load();
        LOGGER.info("AnimatedDoors Version: {} ({})", version, loaderName);
    }
}
