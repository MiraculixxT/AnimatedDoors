package de.miraculixx.animated_doors.client;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import java.util.List;

final class FenceGateAnimationType implements AnimatedBlockType {
    @Override
    public boolean supports(BlockState state) {
        return state.getBlock() instanceof FenceGateBlock && state.hasProperty(FenceGateBlock.OPEN);
    }

    @Override
    public boolean hasOpenChanged(BlockState oldState, BlockState newState) {
        return oldState.getBlock().getClass() == newState.getBlock().getClass()
            && oldState.getValue(FenceGateBlock.OPEN) != newState.getValue(FenceGateBlock.OPEN);
    }

    @Override
    public boolean isOpen(BlockState state) {
        return state.getValue(FenceGateBlock.OPEN);
    }

    @Override
    public BlockPos normalize(BlockGetter level, BlockPos pos, BlockState state) {
        return pos.immutable();
    }

    @Override
    public List<BlockPos> affectedPositions(BlockGetter level, BlockPos normalizedPos, BlockState state) {
        return List.of(normalizedPos.immutable());
    }

    @Override
    public List<RenderInstruction> renderInstructions(AnimationInstance animation, BlockPos pos) {
        BlockState baseState = animation.oldState.setValue(FenceGateBlock.OPEN, false);
        Direction facing = baseState.getValue(HorizontalDirectionalBlock.FACING);
        float amount = animation.openAmount(System.nanoTime());
        float angle = amount * ((float) Math.PI / 2.0f);
        java.util.ArrayList<RenderInstruction> instructions = new java.util.ArrayList<>(3);

        instructions.add(new RenderInstruction(baseState, new Matrix4f(), quad -> isPost(quad, facing)));

        if (facing.getAxis() == Direction.Axis.Z) {
            float sign = facing == Direction.SOUTH ? 1.0f : -1.0f;
            instructions.add(new RenderInstruction(baseState, AnimationMath.rotateY(0.125f, 0.5f, angle * sign), quad -> isLeftLeaf(quad, facing)));
            instructions.add(new RenderInstruction(baseState, AnimationMath.rotateY(0.875f, 0.5f, -angle * sign), quad -> isRightLeaf(quad, facing)));
        } else {
            float sign = facing == Direction.WEST ? 1.0f : -1.0f;
            instructions.add(new RenderInstruction(baseState, AnimationMath.rotateY(0.5f, 0.125f, -angle * sign), quad -> isLeftLeaf(quad, facing)));
            instructions.add(new RenderInstruction(baseState, AnimationMath.rotateY(0.5f, 0.875f, angle * sign), quad -> isRightLeaf(quad, facing)));
        }
        return instructions;
    }

    private boolean isPost(BakedQuad quad, Direction facing) {
        float width = averageWidth(quad, facing);
        return width < 0.125f || width > 0.875f;
    }

    private boolean isLeftLeaf(BakedQuad quad, Direction facing) {
        float width = averageWidth(quad, facing);
        return width >= 0.125f && width < 0.5f;
    }

    private boolean isRightLeaf(BakedQuad quad, Direction facing) {
        float width = averageWidth(quad, facing);
        return width > 0.5f && width <= 0.875f;
    }

    private float averageWidth(BakedQuad quad, Direction facing) {
        float total = 0.0f;
        for (int i = 0; i < BakedQuad.VERTEX_COUNT; i++) {
            Vector3fc position = quad.position(i);
            total += facing.getAxis() == Direction.Axis.Z ? position.x() : position.z();
        }
        return total / BakedQuad.VERTEX_COUNT;
    }
}
