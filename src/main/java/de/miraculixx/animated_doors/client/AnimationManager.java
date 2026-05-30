package de.miraculixx.animated_doors.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

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
        if (type == null || !type.hasOpenChanged(oldState, newState)) {
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
        for (BlockPos pos : animation.affectedPositions) {
            HIDDEN_POSITIONS.remove(pos.asLong());
            markDirty(Minecraft.getInstance().level, pos);
        }
    }

    public static void tick() {
        long now = System.nanoTime();
        for (AnimationInstance animation : activeAnimations()) {
            if (animation.isFinished(now)) {
                complete(animation);
            } else {
                for (BlockPos pos : animation.affectedPositions) {
                    markDirty(Minecraft.getInstance().level, pos);
                }
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

    private static void cancelIfReplaced(BlockGetter level, BlockPos pos, BlockState oldState, BlockState newState) {
        AnimatedBlockType oldType = findType(oldState);
        if (oldType == null || oldState.getBlock() == newState.getBlock()) {
            return;
        }

        BlockPos normalized = oldType.normalize(level, pos, oldState);
        AnimationInstance removed = ACTIVE.remove(normalized.asLong());
        if (removed != null) {
            for (BlockPos affectedPos : removed.affectedPositions) {
                HIDDEN_POSITIONS.remove(affectedPos.asLong());
                markDirty(level, affectedPos);
            }
        }
    }

    private static void markDirty(BlockGetter level, BlockPos pos) {
        ClientLevel clientLevel = level instanceof ClientLevel ? (ClientLevel) level : Minecraft.getInstance().level;
        if (clientLevel != null) {
            clientLevel.setBlocksDirty(pos, clientLevel.getBlockState(pos), clientLevel.getBlockState(pos));
        }
    }
}
