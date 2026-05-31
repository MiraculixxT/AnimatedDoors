package de.miraculixx.animated_doors.client.animation;

import de.miraculixx.animated_doors.client.animation.type.AnimatedBlockType;
import de.miraculixx.animated_doors.client.config.AnimatedDoorsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class AnimationInstance {
    static final long REVEAL_LEAD_NANOS = 25_000_000L;

    public final AnimatedBlockType type;
    public final BlockPos normalizedPos;
    public final BlockState oldState;
    public final BlockState newState;
    public final List<BlockPos> affectedPositions;
    public final float fromOpenAmount;
    public final float toOpenAmount;
    public final long startedAtNanos;
    private final long durationNanos;
    private final long revealLeadNanos;
    private final AnimatedDoorsConfig.Easing easing;
    boolean revealScheduled;

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
        this.durationNanos = AnimatedDoorsConfig.instance().durationNanos();
        this.revealLeadNanos = Math.max(0L, Math.min(REVEAL_LEAD_NANOS, durationNanos - 1L));
        this.easing = AnimatedDoorsConfig.instance().easing();
    }

    public float openAmount(long nowNanos) {
        float raw = (nowNanos - startedAtNanos) / (float) durationNanos;
        float eased = easing.apply(raw);
        return fromOpenAmount + (toOpenAmount - fromOpenAmount) * eased;
    }

    public boolean isFinished(long nowNanos) {
        return nowNanos - startedAtNanos >= durationNanos;
    }

    boolean shouldRevealOriginal(long nowNanos) {
        return !revealScheduled && nowNanos - startedAtNanos >= durationNanos - revealLeadNanos;
    }
}
