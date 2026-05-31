package de.miraculixx.animated_doors.client.interaction;

import de.miraculixx.animated_doors.client.config.AnimatedDoorsConfig;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class ConnectedBlockInteractionHandler {
    private static boolean syntheticUse;

    private ConnectedBlockInteractionHandler() {
    }

    public static void init() {
        UseBlockCallback.EVENT.register(ConnectedBlockInteractionHandler::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (syntheticUse || !level.isClientSide() || !(player instanceof LocalPlayer localPlayer)) {
            return InteractionResult.PASS;
        }

        Minecraft minecraft = Minecraft.getInstance();
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (gameMode == null || minecraft.player != localPlayer) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        List<BlockPos> connected = connectedPositions(level, pos, state);
        if (connected.isEmpty()) {
            return InteractionResult.PASS;
        }

        boolean targetOpen = !state.getValue(BlockStateProperties.OPEN);
        syntheticUse = true;
        try {
            for (BlockPos connectedPos : connected) {
                BlockState connectedState = level.getBlockState(connectedPos);
                if (connectedState.hasProperty(BlockStateProperties.OPEN)
                    && connectedState.getValue(BlockStateProperties.OPEN) != targetOpen) {
                    gameMode.useItemOn(localPlayer, hand, syntheticHit(hitResult, connectedPos));
                }
            }
        } finally {
            syntheticUse = false;
        }

        return InteractionResult.PASS;
    }

    private static List<BlockPos> connectedPositions(Level level, BlockPos pos, BlockState state) {
        if (isDoor(state)) {
            if (!AnimatedDoorsConfig.instance().connectedDoorsEnabled()) {
                return List.of();
            }
            BlockPos basePos = doorBasePos(pos, state);
            BlockState baseState = level.getBlockState(basePos);
            BlockPos connectedDoor = connectedDoor(level, basePos, baseState);
            return connectedDoor == null ? List.of() : List.of(connectedDoor);
        }
        if (isTrapdoor(state)) {
            if (!AnimatedDoorsConfig.instance().connectedTrapdoorsEnabled()) {
                return List.of();
            }
            return connectedTrapdoors(level, pos, state);
        }
        if (isFenceGate(state)) {
            if (!AnimatedDoorsConfig.instance().connectedFenceGatesEnabled()) {
                return List.of();
            }
            return connectedFenceGates(level, pos, state);
        }
        return List.of();
    }

    private static BlockPos connectedDoor(Level level, BlockPos basePos, BlockState state) {
        if (!isDoor(state) || state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
            return null;
        }

        Direction facing = state.getValue(DoorBlock.FACING);
        DoorHingeSide hinge = state.getValue(DoorBlock.HINGE);
        BlockPos connectedPos = basePos.relative(hinge == DoorHingeSide.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
        BlockState connected = level.getBlockState(connectedPos);

        if (!isDoor(connected)
            || connected.getBlock() != state.getBlock()
            || connected.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER
            || connected.getValue(DoorBlock.FACING) != facing
            || connected.getValue(DoorBlock.HINGE) == hinge) {
            return null;
        }

        return connectedPos;
    }

    private static List<BlockPos> connectedTrapdoors(Level level, BlockPos pos, BlockState state) {
        if (!isTrapdoor(state)) {
            return List.of();
        }

        Direction facing = state.getValue(TrapDoorBlock.FACING);
        List<BlockPos> connected = new ArrayList<>(3);
        addTrapdoorIfConnected(level, pos.relative(facing), state, facing.getOpposite(), connected);
        addTrapdoorIfConnected(level, pos.relative(facing.getClockWise()), state, facing, connected);
        addTrapdoorIfConnected(level, pos.relative(facing.getCounterClockWise()), state, facing, connected);
        return connected;
    }

    private static void addTrapdoorIfConnected(Level level, BlockPos pos, BlockState source, Direction requiredFacing, List<BlockPos> connected) {
        BlockState candidate = level.getBlockState(pos);
        if (isTrapdoor(candidate)
            && candidate.getBlock() == source.getBlock()
            && candidate.getValue(TrapDoorBlock.HALF) == source.getValue(TrapDoorBlock.HALF)
            && candidate.getValue(TrapDoorBlock.FACING) == requiredFacing) {
            connected.add(pos.immutable());
        }
    }

    private static List<BlockPos> connectedFenceGates(Level level, BlockPos pos, BlockState state) {
        if (!isFenceGate(state)) {
            return List.of();
        }

        Direction facing = state.getValue(FenceGateBlock.FACING);
        List<BlockPos> connected = new ArrayList<>(2);
        addFenceGateIfConnected(level, pos.relative(facing.getClockWise()), state, connected);
        addFenceGateIfConnected(level, pos.relative(facing.getCounterClockWise()), state, connected);
        return connected;
    }

    private static void addFenceGateIfConnected(Level level, BlockPos pos, BlockState source, List<BlockPos> connected) {
        BlockState candidate = level.getBlockState(pos);
        if (isFenceGate(candidate)
            && candidate.getBlock() == source.getBlock()
            && candidate.getValue(FenceGateBlock.FACING) == source.getValue(FenceGateBlock.FACING)) {
            connected.add(pos.immutable());
        }
    }

    private static BlockPos doorBasePos(BlockPos pos, BlockState state) {
        return state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below().immutable() : pos.immutable();
    }

    private static BlockHitResult syntheticHit(BlockHitResult sourceHit, BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos), sourceHit.getDirection(), pos, false);
    }

    private static boolean isDoor(BlockState state) {
        return state.getBlock() instanceof DoorBlock
            && state.hasProperty(DoorBlock.OPEN)
            && state.hasProperty(DoorBlock.FACING)
            && state.hasProperty(DoorBlock.HINGE)
            && state.hasProperty(DoorBlock.HALF);
    }

    private static boolean isTrapdoor(BlockState state) {
        return state.getBlock() instanceof TrapDoorBlock
            && state.hasProperty(TrapDoorBlock.OPEN)
            && state.hasProperty(TrapDoorBlock.FACING)
            && state.hasProperty(TrapDoorBlock.HALF);
    }

    private static boolean isFenceGate(BlockState state) {
        return state.getBlock() instanceof FenceGateBlock
            && state.hasProperty(FenceGateBlock.OPEN)
            && state.hasProperty(FenceGateBlock.FACING);
    }
}
