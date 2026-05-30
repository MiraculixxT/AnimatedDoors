package de.miraculixx.animated_doors.mixin.compat;

import de.miraculixx.animated_doors.client.animation.AnimationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
    targets = "net.caffeinemc.mods.sodium.client.world.LevelSlice",
    remap = false
)
public abstract class SodiumLevelSliceMixin {
    @Inject(
        method = "getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void animated_doors$hideAnimatedBlock(int x, int y, int z, CallbackInfoReturnable<BlockState> cir) {
        if (AnimationManager.shouldHide(new BlockPos(x, y, z))) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}
