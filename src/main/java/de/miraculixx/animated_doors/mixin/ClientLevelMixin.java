package de.miraculixx.animated_doors.mixin;

import de.miraculixx.animated_doors.client.AnimationManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Unique
    private final Deque<animated_doors$TrackedChange> animated_doors$changes = new ArrayDeque<>();

    @Inject(method = "setBlock", at = @At("HEAD"))
    private void animated_doors$captureSetBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        ClientLevel self = (ClientLevel) (Object) this;
        animated_doors$changes.push(new animated_doors$TrackedChange(pos.immutable(), self.getBlockState(pos)));
    }

    @Inject(method = "setBlock", at = @At("RETURN"))
    private void animated_doors$afterSetBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        animated_doors$TrackedChange change = animated_doors$changes.pop();
        if (!cir.getReturnValueZ()) {
            return;
        }
        ClientLevel self = (ClientLevel) (Object) this;
        AnimationManager.onBlockStateChanged(self, change.pos, change.oldState, self.getBlockState(change.pos));
    }

    @Inject(method = "setServerVerifiedBlockState", at = @At("HEAD"))
    private void animated_doors$captureVerified(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        ClientLevel self = (ClientLevel) (Object) this;
        animated_doors$changes.push(new animated_doors$TrackedChange(pos.immutable(), self.getBlockState(pos)));
    }

    @Inject(method = "setServerVerifiedBlockState", at = @At("RETURN"))
    private void animated_doors$afterVerified(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        animated_doors$TrackedChange change = animated_doors$changes.pop();
        ClientLevel self = (ClientLevel) (Object) this;
        AnimationManager.onBlockStateChanged(self, change.pos, change.oldState, self.getBlockState(change.pos));
    }

    @Unique
    private record animated_doors$TrackedChange(BlockPos pos, BlockState oldState) {
    }
}
