package de.miraculixx.animated_doors.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import de.miraculixx.animated_doors.client.animation.AnimatedDoorRenderer;
import de.miraculixx.animated_doors.client.animation.AnimationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Shadow
    @Final
    private SubmitNodeStorage submitNodeStorage;

    @Unique
    private PoseStack animated_doors$poseStack;

    @ModifyExpressionValue(method = "lambda$addMainPass$0", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/vertex/PoseStack;"))
    private PoseStack animated_doors$capturePoseStack(PoseStack poseStack) {
        animated_doors$poseStack = poseStack;
        return poseStack;
    }

    @Inject(
        method = "lambda$addMainPass$0",
        at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=renderSolidFeatures")
    )
    private void animated_doors$collectSubmits(CallbackInfo ci) {
        if (minecraft.level == null || animated_doors$poseStack == null) {
            return;
        }

        AnimationManager.tick();
        AnimatedDoorRenderer.render(animated_doors$poseStack, submitNodeStorage, levelRenderState.cameraRenderState.pos);
    }

    @Inject(method = "lambda$addMainPass$0", at = @At("RETURN"))
    private void animated_doors$clearPoseStack(CallbackInfo ci) {
        animated_doors$poseStack = null;
    }
}
