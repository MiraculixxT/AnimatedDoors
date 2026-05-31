package de.miraculixx.animated_doors.client;

import com.mojang.logging.LogUtils;
import de.miraculixx.animated_doors.client.animation.AnimatedDoorRenderer;
import de.miraculixx.animated_doors.client.config.AnimatedDoorsConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

public class AnimatedDoors implements ClientModInitializer {
    final static String MOD_ID = "animated_doors";
    final static Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        AnimatedDoorsConfig.instance().load();
        var fabricLoader = FabricLoader.getInstance();
        var instance = fabricLoader.getModContainer(MOD_ID).orElseThrow();
        LOGGER.info("AnimatedDoors Version: "+instance.getMetadata().getVersion()+" (fabric)");
        AnimatedDoorRenderer.init();
    }
}
