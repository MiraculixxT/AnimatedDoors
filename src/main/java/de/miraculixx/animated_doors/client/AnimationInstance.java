package de.miraculixx.animated_doors.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class AnimationInstance {
    static final long DURATION_NANOS = 500_000_000L;

    public final AnimatedBlockType type;
    final BlockPos normalizedPos;
    final BlockState oldState;
    final BlockState newState;
    final List<BlockPos> affectedPositions;
    final float fromOpenAmount;
    final float toOpenAmount;
    final long startedAtNanos;

    AnimationInstance(
        AnimatedBlockType type,
        BlockPos normalizedPos,
        BlockState oldState,
        BlockState newState,
        List<BlockPos> affectedPositions,
        float fromOpenAmount,
        float toOpenAmount,
        long startedAtNanos
    ) {
        this.type = type;
        this.normalizedPos = normalizedPos.immutable();
        this.oldState = oldState;
        this.newState = newState;
        this.affectedPositions = List.copyOf(affectedPositions);
        this.fromOpenAmount = fromOpenAmount;
        this.toOpenAmount = toOpenAmount;
        this.startedAtNanos = startedAtNanos;
    }

    float openAmount(long nowNanos) {
        float raw = (nowNanos - startedAtNanos) / (float) DURATION_NANOS;
        float eased = AnimationMath.smooth(raw);
        return fromOpenAmount + (toOpenAmount - fromOpenAmount) * eased;
    }

    boolean isFinished(long nowNanos) {
        return nowNanos - startedAtNanos >= DURATION_NANOS;
    }
}
