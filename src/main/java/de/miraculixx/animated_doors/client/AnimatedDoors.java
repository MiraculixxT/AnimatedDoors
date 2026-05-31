package de.miraculixx.animated_doors.client;

import de.miraculixx.animated_doors.client.interaction.ConnectedBlockInteractionHandler;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class AnimatedDoors implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricLoader fabricLoader = FabricLoader.getInstance();
        String version = fabricLoader.getModContainer(AnimatedDoorsClient.MOD_ID)
            .orElseThrow()
            .getMetadata()
            .getVersion()
            .getFriendlyString();

        AnimatedDoorsClient.init("fabric", version, fabricLoader.getConfigDir());
        UseBlockCallback.EVENT.register(ConnectedBlockInteractionHandler::onUseBlock);
    }
}
