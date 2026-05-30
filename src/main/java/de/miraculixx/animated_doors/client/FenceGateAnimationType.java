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
    private static final float EDGE_CENTER = 1.0f / 16.0f;
    private static final float FAR_EDGE_CENTER = 15.0f / 16.0f;
    private static final float CENTER = 0.5f;
    private static final float HALF_THICKNESS = 1.0f / 16.0f;
    private static final float EPSILON = 0.0001f;

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
        BlockState baseState = animation.newState.setValue(FenceGateBlock.OPEN, false);
        Direction facing = baseState.getValue(HorizontalDirectionalBlock.FACING);
        float amount = animation.openAmount(System.nanoTime());
        float angle = amount * ((float) Math.PI / 2.0f);
        java.util.ArrayList<RenderInstruction> instructions = new java.util.ArrayList<>(3);

        instructions.add(new RenderInstruction(baseState, new Matrix4f(), quad -> isPost(quad, facing)));

        if (facing.getAxis() == Direction.Axis.Z) {
            float sign = facing == Direction.SOUTH ? -1.0f : 1.0f;
            instructions.add(new RenderInstruction(baseState, AnimationMath.rotateY(EDGE_CENTER, CENTER, angle * sign), quad -> isLowLeaf(quad, facing)));
            instructions.add(new RenderInstruction(baseState, AnimationMath.rotateY(FAR_EDGE_CENTER, CENTER, -angle * sign), quad -> isHighLeaf(quad, facing)));
        } else {
            float sign = facing == Direction.WEST ? 1.0f : -1.0f;
            instructions.add(new RenderInstruction(baseState, AnimationMath.rotateY(CENTER, EDGE_CENTER, -angle * sign), quad -> isLowLeaf(quad, facing)));
            instructions.add(new RenderInstruction(baseState, AnimationMath.rotateY(CENTER, FAR_EDGE_CENTER, angle * sign), quad -> isHighLeaf(quad, facing)));
        }
        return instructions;
    }

    private boolean isPost(BakedQuad quad, Direction facing) {
        float width = averageWidth(quad, facing);
        return isEdge(width) && isCenteredDepth(quad, facing);
    }

    private boolean isLowLeaf(BakedQuad quad, Direction facing) {
        float width = averageWidth(quad, facing);
        return width < CENTER && !isPost(quad, facing);
    }

    private boolean isHighLeaf(BakedQuad quad, Direction facing) {
        float width = averageWidth(quad, facing);
        return width > CENTER && !isPost(quad, facing);
    }

    private boolean isEdge(float width) {
        return width < 0.25f || width > 0.75f;
    }

    private float averageWidth(BakedQuad quad, Direction facing) {
        float total = 0.0f;
        for (int i = 0; i < BakedQuad.VERTEX_COUNT; i++) {
            Vector3fc position = quad.position(i);
            total += facing.getAxis() == Direction.Axis.Z ? position.x() : position.z();
        }
        return total / BakedQuad.VERTEX_COUNT;
    }

    private boolean isCenteredDepth(BakedQuad quad, Direction facing) {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < BakedQuad.VERTEX_COUNT; i++) {
            Vector3fc position = quad.position(i);
            float depth = facing.getAxis() == Direction.Axis.Z ? position.z() : position.x();
            min = Math.min(min, depth);
            max = Math.max(max, depth);
        }
        return min >= CENTER - HALF_THICKNESS - EPSILON && max <= CENTER + HALF_THICKNESS + EPSILON;
    }
}
