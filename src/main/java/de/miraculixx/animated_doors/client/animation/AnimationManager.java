package de.miraculixx.animated_doors.client.animation;

import de.miraculixx.animated_doors.client.animation.type.AnimatedBlockType;
import de.miraculixx.animated_doors.client.animation.type.DoorAnimationType;
import de.miraculixx.animated_doors.client.animation.type.FenceGateAnimationType;
import de.miraculixx.animated_doors.client.animation.type.TrapdoorAnimationType;
import de.miraculixx.animated_doors.client.config.AnimatedDoorsConfig;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class AnimationManager {
    private static final List<AnimatedBlockType> TYPES = List.of(
        new DoorAnimationType(),
        new TrapdoorAnimationType(),
        new FenceGateAnimationType()
    );

    private static final Long2ObjectOpenHashMap<AnimationInstance> ACTIVE = new Long2ObjectOpenHashMap<>();
    private static final LongOpenHashSet HIDDEN_POSITIONS = new LongOpenHashSet();

    private AnimationManager() {
    }

    public static void onBlockStateChanged(BlockGetter level, BlockPos pos, BlockState oldState, BlockState newState) {
        if (oldState == null || newState == null || oldState == newState) {
            return;
        }

        AnimatedBlockType type = findType(oldState, newState);
        if (type == null) {
            cancelIfReplaced(level, pos, oldState, newState);
            return;
        }
        if (!isEnabled(type)) {
            cancelActive(level, pos, newState, type);
            return;
        }
        if (!type.hasOpenChanged(oldState, newState)) {
            cancelIfReplaced(level, pos, oldState, newState);
            return;
        }

        BlockPos normalizedPos = type.normalize(level, pos, newState);
        long key = normalizedPos.asLong();
        long now = System.nanoTime();
        AnimationInstance previous = ACTIVE.get(key);
        float from = previous == null ? (type.isOpen(oldState) ? 1.0f : 0.0f) : previous.openAmount(now);
        float to = type.isOpen(newState) ? 1.0f : 0.0f;

        List<BlockPos> affected = type.affectedPositions(level, normalizedPos, newState);
        AnimationInstance animation = new AnimationInstance(type, normalizedPos, oldState, newState, affected, from, to, now);
        ACTIVE.put(key, animation);
        for (BlockPos affectedPos : affected) {
            HIDDEN_POSITIONS.add(affectedPos.asLong());
            markDirty(level, affectedPos);
        }
    }

    public static boolean shouldHide(BlockPos pos, BlockState state) {
        return HIDDEN_POSITIONS.contains(pos.asLong()) && findType(state) != null;
    }

    public static boolean shouldHide(BlockPos pos) {
        return HIDDEN_POSITIONS.contains(pos.asLong());
    }

    public static AnimationInstance animationAt(BlockPos pos, BlockState state) {
        if (!HIDDEN_POSITIONS.contains(pos.asLong()) || findType(state) == null) {
            return null;
        }

        for (AnimationInstance animation : ACTIVE.values()) {
            for (BlockPos affectedPos : animation.affectedPositions) {
                if (affectedPos.equals(pos)) {
                    return animation;
                }
            }
        }
        return null;
    }

    static Collection<AnimationInstance> activeAnimations() {
        return new ArrayList<>(ACTIVE.values());
    }

    public static void complete(AnimationInstance animation) {
        ACTIVE.remove(animation.normalizedPos.asLong());
        if (!animation.revealScheduled) {
            revealOriginal(animation);
        }
    }

    public static void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            clear();
            return;
        }

        long now = System.nanoTime();
        for (AnimationInstance animation : activeAnimations()) {
            if (!isStillPresent(level, animation)) {
                cancel(animation, level);
            } else if (animation.isFinished(now)) {
                complete(animation);
            } else if (animation.shouldRevealOriginal(now)) {
                revealOriginal(animation);
            }
        }
    }

    private static AnimatedBlockType findType(BlockState oldState, BlockState newState) {
        for (AnimatedBlockType type : TYPES) {
            if (type.supports(oldState) && type.supports(newState)) {
                return type;
            }
        }
        return null;
    }

    private static AnimatedBlockType findType(BlockState state) {
        for (AnimatedBlockType type : TYPES) {
            if (type.supports(state)) {
                return type;
            }
        }
        return null;
    }

    private static boolean isEnabled(AnimatedBlockType type) {
        AnimatedDoorsConfig config = AnimatedDoorsConfig.instance();
        if (type instanceof DoorAnimationType) {
            return config.doorsEnabled();
        }
        if (type instanceof TrapdoorAnimationType) {
            return config.trapdoorsEnabled();
        }
        if (type instanceof FenceGateAnimationType) {
            return config.fenceGatesEnabled();
        }
        return true;
    }

    private static void cancelIfReplaced(BlockGetter level, BlockPos pos, BlockState oldState, BlockState newState) {
        AnimatedBlockType oldType = findType(oldState);
        if (oldType == null) {
            AnimationInstance removed = removeAnimationAt(pos);
            if (removed != null) {
                revealOriginal(removed);
            }
            return;
        }
        if (oldState.getBlock() == newState.getBlock()) {
            return;
        }

        BlockPos normalized = oldType.normalize(level, pos, oldState);
        AnimationInstance removed = ACTIVE.remove(normalized.asLong());
        if (removed == null) {
            removed = removeAnimationAt(pos);
        }
        if (removed != null) {
            revealOriginal(removed);
        }
    }

    private static void cancelActive(BlockGetter level, BlockPos pos, BlockState state, AnimatedBlockType type) {
        BlockPos normalized = type.normalize(level, pos, state);
        AnimationInstance removed = ACTIVE.remove(normalized.asLong());
        if (removed != null && !removed.revealScheduled) {
            revealOriginal(removed);
        }
    }

    private static void revealOriginal(AnimationInstance animation) {
        animation.revealScheduled = true;
        for (BlockPos pos : animation.affectedPositions) {
            HIDDEN_POSITIONS.remove(pos.asLong());
            markDirty(Minecraft.getInstance().level, pos);
        }
    }

    private static boolean isStillPresent(ClientLevel level, AnimationInstance animation) {
        for (BlockPos pos : animation.affectedPositions) {
            BlockState state = level.getBlockState(pos);
            if (!animation.type.supports(state) || state.getBlock() != animation.newState.getBlock()) {
                return false;
            }
        }
        return true;
    }

    private static void cancel(AnimationInstance animation, BlockGetter level) {
        ACTIVE.remove(animation.normalizedPos.asLong());
        for (BlockPos pos : animation.affectedPositions) {
            HIDDEN_POSITIONS.remove(pos.asLong());
            markDirty(level, pos);
        }
    }

    private static AnimationInstance removeAnimationAt(BlockPos pos) {
        AnimationInstance direct = ACTIVE.remove(pos.asLong());
        if (direct != null) {
            return direct;
        }

        for (AnimationInstance animation : activeAnimations()) {
            for (BlockPos affectedPos : animation.affectedPositions) {
                if (affectedPos.equals(pos)) {
                    ACTIVE.remove(animation.normalizedPos.asLong());
                    return animation;
                }
            }
        }
        return null;
    }

    private static void clear() {
        ACTIVE.clear();
        HIDDEN_POSITIONS.clear();
    }

    private static void markDirty(BlockGetter level, BlockPos pos) {
        ClientLevel clientLevel = level instanceof ClientLevel ? (ClientLevel) level : Minecraft.getInstance().level;
        Minecraft minecraft = Minecraft.getInstance();
        if (clientLevel != null && minecraft.levelRenderer != null) {
            BlockState state = clientLevel.getBlockState(pos);
            if (state.hasProperty(BlockStateProperties.OPEN)) {
                BlockState flipped = state.cycle(BlockStateProperties.OPEN);
                minecraft.levelRenderer.blockChanged(clientLevel, pos, state, flipped, 3);
                minecraft.levelRenderer.blockChanged(clientLevel, pos, flipped, state, 3);
            } else {
                minecraft.levelRenderer.blockChanged(clientLevel, pos, state, state, 3);
            }
        }
    }
}
