package de.miraculixx.animated_doors.mixin.compat;

import de.miraculixx.animated_doors.client.animation.AnimationInstance;
import de.miraculixx.animated_doors.client.animation.AnimationManager;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer", remap = false)
public abstract class SodiumBlockRendererMixin {
    @Inject(
        method = "renderModel(Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void animated_doors$hideAnimatedBlock(
        BlockStateModel model,
        BlockState state,
        BlockPos pos,
        BlockPos origin,
        CallbackInfo ci
    ) {
        AnimationInstance animation = AnimationManager.animationAt(pos, state);
        if (animation != null) {
            ci.cancel();
        }
    }
}
