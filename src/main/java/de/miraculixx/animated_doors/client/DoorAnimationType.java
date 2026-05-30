package de.miraculixx.animated_doors.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.joml.Matrix4f;

import java.util.List;

final class DoorAnimationType implements AnimatedBlockType {
    @Override
    public boolean supports(BlockState state) {
        return state.getBlock() instanceof DoorBlock && state.hasProperty(DoorBlock.OPEN);
    }

    @Override
    public boolean hasOpenChanged(BlockState oldState, BlockState newState) {
        return oldState.getBlock().getClass() == newState.getBlock().getClass()
            && oldState.getValue(DoorBlock.OPEN) != newState.getValue(DoorBlock.OPEN);
    }

    @Override
    public boolean isOpen(BlockState state) {
        return state.getValue(DoorBlock.OPEN);
    }

    @Override
    public BlockPos normalize(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below().immutable() : pos.immutable();
    }

    @Override
    public List<BlockPos> affectedPositions(BlockGetter level, BlockPos normalizedPos, BlockState state) {
        return List.of(normalizedPos.immutable(), normalizedPos.above().immutable());
    }

    @Override
    public List<RenderInstruction> renderInstructions(AnimationInstance animation, BlockPos pos) {
        float amount = animation.openAmount(System.nanoTime());
        BlockState state = animation.oldState.setValue(DoorBlock.OPEN, false);
        if (pos.getY() != animation.normalizedPos.getY()) {
            state = state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        } else {
            state = state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        }
        return List.of(new RenderInstruction(state, transform(state, amount)));
    }

    private Matrix4f transform(BlockState baseState, float amount) {
        DoorHingeSide hinge = baseState.getValue(DoorBlock.HINGE);
        Direction facing = baseState.getValue(DoorBlock.FACING);
        float angle = amount * ((float) Math.PI / 2.0f) * (hinge == DoorHingeSide.LEFT ? 1.0f : -1.0f);
        float x = hingeX(facing, hinge);
        float z = hingeZ(facing, hinge);
        return AnimationMath.rotateY(x, z, angle);
    }

    private float hingeX(Direction facing, DoorHingeSide hinge) {
        return switch (facing) {
            case EAST -> 0.0f;
            case WEST -> 1.0f;
            case NORTH -> hinge == DoorHingeSide.LEFT ? 0.0f : 1.0f;
            case SOUTH -> hinge == DoorHingeSide.LEFT ? 1.0f : 0.0f;
            default -> 0.0f;
        };
    }

    private float hingeZ(Direction facing, DoorHingeSide hinge) {
        return switch (facing) {
            case NORTH -> 1.0f;
            case SOUTH -> 0.0f;
            case EAST -> hinge == DoorHingeSide.LEFT ? 0.0f : 1.0f;
            case WEST -> hinge == DoorHingeSide.LEFT ? 1.0f : 0.0f;
            default -> 0.0f;
        };
    }
}
