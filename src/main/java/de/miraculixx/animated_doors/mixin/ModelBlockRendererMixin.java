package de.miraculixx.animated_doors.mixin;

import de.miraculixx.animated_doors.client.animation.AnimationManager;
import de.miraculixx.animated_doors.client.animation.AnimationInstance;
import de.miraculixx.animated_doors.client.animation.GeneratedFace;
import de.miraculixx.animated_doors.client.animation.RenderInstruction;
import de.miraculixx.animated_doors.client.animation.TransformingBlockQuadOutput;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin {
    @Unique
    private static final ThreadLocal<Boolean> animated_doors$renderingAnimation = ThreadLocal.withInitial(() -> false);

    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    private void animated_doors$renderAnimatedBlock(
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
        if (animated_doors$renderingAnimation.get()) {
            return;
        }

        AnimationInstance animation = AnimationManager.animationAt(pos, state);
        if (animation == null) {
            return;
        }

        List<RenderInstruction> instructions = animation.type.renderInstructions(animation, pos);
        ModelBlockRenderer self = (ModelBlockRenderer) (Object) this;
        animated_doors$renderingAnimation.set(true);
        try {
            for (RenderInstruction instruction : instructions) {
                BlockStateModel animatedModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(instruction.state());
                self.tesselateBlock(
                    new TransformingBlockQuadOutput(output, instruction.transform(), instruction.filter()),
                    x,
                    y,
                    z,
                    level,
                    pos,
                    instruction.state(),
                    animatedModel,
                    instruction.state().getSeed(pos)
                );
                if (!instruction.generatedFaces().isEmpty()) {
                    BakedQuad template = GeneratedFace.findTemplate(animatedModel, instruction.state(), pos);
                    if (template != null) {
                        TransformingBlockQuadOutput generatedOutput = new TransformingBlockQuadOutput(output, instruction.transform(), quad -> true);
                        for (GeneratedFace face : instruction.generatedFaces()) {
                            generatedOutput.put(x, y, z, face.bake(template), new QuadInstance());
                        }
                    }
                }
            }
        } finally {
            animated_doors$renderingAnimation.set(false);
        }
        ci.cancel();
    }
}
