package de.miraculixx.animated_doors

import com.mojang.logging.LogUtils
import de.miraculixx.animated_doors.client.animation.AnimatedDoorRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import org.slf4j.Logger

class AnimatedDoors : ClientModInitializer {

    companion object {
        val MOD_ID = "animated_doors"
        val LOGGER: Logger = LogUtils.getLogger()
        lateinit var INSTANCE: ModContainer
    }

    override fun onInitializeClient() {
        val fabricLoader = FabricLoader.getInstance()
        INSTANCE = fabricLoader.getModContainer(MOD_ID).get()
        LOGGER.info("AnimatedDoors Version: ${INSTANCE.metadata.version} (fabric)")
        AnimatedDoorRenderer.init()
    }
}
