package de.miraculixx.animated_doors.client;

import com.mojang.logging.LogUtils;
import de.miraculixx.animated_doors.client.config.AnimatedDoorsConfig;
import de.miraculixx.animated_doors.client.update.UpdateManager;
import net.minecraft.SharedConstants;
import org.slf4j.Logger;

import java.nio.file.Path;

public final class AnimatedDoorsClient {
    public static final String MOD_ID = "animated_doors";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean sodiumCompatLoaded;

    private AnimatedDoorsClient() {
    }

    public static void init(String loaderName, String version, Path configDir, boolean sodiumCompatLoaded) {
        AnimatedDoorsClient.sodiumCompatLoaded = sodiumCompatLoaded;
        AnimatedDoorsConfig.instance().configure(configDir);
        AnimatedDoorsConfig.instance().load();
        LOGGER.info("AnimatedDoors Version: {} ({})", version, loaderName);
        UpdateManager.startUpdateChecker(
            loaderName,
            SharedConstants.getCurrentVersion().name(),
            version,
            configDir
        );
    }

    public static boolean sodiumCompatLoaded() {
        return sodiumCompatLoaded;
    }
}
