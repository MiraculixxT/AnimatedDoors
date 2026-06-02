package de.miraculixx.animated_doors.client.neoforge;

import de.miraculixx.animated_doors.client.AnimatedDoorsClient;
import de.miraculixx.animated_doors.client.config.AnimatedDoorsConfigScreen;
import de.miraculixx.animated_doors.client.interaction.ConnectedBlockInteractionHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.function.Supplier;

@Mod(value = AnimatedDoorsClient.MOD_ID, dist = Dist.CLIENT)
public final class NeoForgeAnimatedDoors {
    private static final String SODIUM_NEO_FORK_MOD_ID = "embeddium";

    public NeoForgeAnimatedDoors(IEventBus modBus, ModContainer container) {
        AnimatedDoorsClient.init(
            "neoforge",
            container.getModInfo().getVersion().toString(),
            FMLPaths.CONFIGDIR.get(),
            ModList.get().isLoaded("sodium") || ModList.get().isLoaded(SODIUM_NEO_FORK_MOD_ID)
        );

        container.registerExtensionPoint(
            IConfigScreenFactory.class,
            (Supplier<IConfigScreenFactory>) () -> (modContainer, parent) -> new AnimatedDoorsConfigScreen(parent)
        );
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, this::onRightClickBlock);
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ConnectedBlockInteractionHandler.onUseBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
    }
}
