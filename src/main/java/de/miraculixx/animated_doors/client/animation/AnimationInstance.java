package de.miraculixx.animated_doors.client.animation;

import de.miraculixx.animated_doors.client.animation.type.AnimatedBlockType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class AnimationInstance {
    static final long DURATION_NANOS = 500_000_000L;

    public final AnimatedBlockType type;
    public final BlockPos normalizedPos;
    public final BlockState oldState;
    public final BlockState newState;
    public final List<BlockPos> affectedPositions;
    public final float fromOpenAmount;
    public final float toOpenAmount;
    public final long startedAtNanos;

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

    public float openAmount(long nowNanos) {
        float raw = (nowNanos - startedAtNanos) / (float) DURATION_NANOS;
        float eased = AnimationMath.smooth(raw);
        return fromOpenAmount + (toOpenAmount - fromOpenAmount) * eased;
    }

    public boolean isFinished(long nowNanos) {
        return nowNanos - startedAtNanos >= DURATION_NANOS;
    }
}
