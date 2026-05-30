package de.miraculixx.animated_doors.mixin;

import de.miraculixx.animated_doors.client.animation.AnimationManager;
import de.miraculixx.animated_doors.client.animation.AnimationInstance;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {
    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    private void animated_doors$hideAnimatedBlock(
        BlockQuadOutput output,
        float x,
        float y,
        float z,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState state,
        BlockStateModel model,
        long seed,
        CallbackInfo ci
    ) {
        AnimationInstance animation = AnimationManager.animationAt(pos, state);
        if (animation == null) {
            return;
        }

        ci.cancel();
    }
}
