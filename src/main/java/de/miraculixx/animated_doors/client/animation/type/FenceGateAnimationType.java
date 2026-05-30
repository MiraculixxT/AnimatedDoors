package de.miraculixx.animated_doors.client.animation.type;

import de.miraculixx.animated_doors.client.animation.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public final class FenceGateAnimationType implements AnimatedBlockType {
    private static final float EDGE_CENTER = 1.0f / 16.0f;
    private static final float FAR_EDGE_CENTER = 15.0f / 16.0f;
    private static final float CENTER = 0.5f;
    private static final float HALF_THICKNESS = 1.0f / 16.0f;
    private static final float GATE_THICKNESS = 2.0f / 16.0f;
    private static final float LOWER_BAR_MIN = 6.0f / 16.0f;
    private static final float LOWER_BAR_MAX = 9.0f / 16.0f;
    private static final float UPPER_BAR_MIN = 12.0f / 16.0f;
    private static final float UPPER_BAR_MAX = 15.0f / 16.0f;
    private static final float WALL_Y_OFFSET = -3.0f / 16.0f;
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
        BlockState openState = animation.newState.setValue(FenceGateBlock.OPEN, true);
        Direction facing = baseState.getValue(HorizontalDirectionalBlock.FACING);
        float amount = animation.openAmount(System.nanoTime());
        float angle = (amount - 1.0f) * ((float) Math.PI / 2.0f);
        java.util.ArrayList<RenderInstruction> instructions = new java.util.ArrayList<>(3);

        instructions.add(new RenderInstruction(baseState, new Matrix4f(), quad -> isPost(quad, facing)));

        if (facing.getAxis() == Direction.Axis.Z) {
            float sign = facing == Direction.SOUTH ? -1.0f : 1.0f;
            instructions.add(new RenderInstruction(openState, AnimationMath.rotateY(EDGE_CENTER, CENTER, angle * sign), quad -> isLowLeaf(quad, facing), hingeCaps(facing, true, baseState.getValue(FenceGateBlock.IN_WALL))));
            instructions.add(new RenderInstruction(openState, AnimationMath.rotateY(FAR_EDGE_CENTER, CENTER, -angle * sign), quad -> isHighLeaf(quad, facing), hingeCaps(facing, false, baseState.getValue(FenceGateBlock.IN_WALL))));
        } else {
            float sign = facing == Direction.WEST ? 1.0f : -1.0f;
            instructions.add(new RenderInstruction(openState, AnimationMath.rotateY(CENTER, EDGE_CENTER, -angle * sign), quad -> isLowLeaf(quad, facing), hingeCaps(facing, true, baseState.getValue(FenceGateBlock.IN_WALL))));
            instructions.add(new RenderInstruction(openState, AnimationMath.rotateY(CENTER, FAR_EDGE_CENTER, angle * sign), quad -> isHighLeaf(quad, facing), hingeCaps(facing, false, baseState.getValue(FenceGateBlock.IN_WALL))));
        }
        return instructions;
    }

    private List<GeneratedFace> hingeCaps(Direction facing, boolean lowLeaf, boolean inWall) {
        float minWidth = lowLeaf ? 0.0f : 1.0f - GATE_THICKNESS;
        float maxWidth = lowLeaf ? GATE_THICKNESS : 1.0f;
        float depth = switch (facing) {
            case SOUTH, EAST -> CENTER + HALF_THICKNESS;
            case NORTH, WEST -> CENTER - HALF_THICKNESS;
            default -> CENTER + HALF_THICKNESS;
        };
        float yOffset = inWall ? WALL_Y_OFFSET : 0.0f;

        ArrayList<GeneratedFace> faces = new ArrayList<>(4);
        addCap(faces, facing, minWidth, maxWidth, LOWER_BAR_MIN + yOffset, LOWER_BAR_MAX + yOffset, depth);
        addCap(faces, facing, minWidth, maxWidth, UPPER_BAR_MIN + yOffset, UPPER_BAR_MAX + yOffset, depth);
        return faces;
    }

    private void addCap(List<GeneratedFace> faces, Direction facing, float minWidth, float maxWidth, float minY, float maxY, float depth) {
        if (facing.getAxis() == Direction.Axis.Z) {
            Vector3f p0 = new Vector3f(minWidth, minY, depth);
            Vector3f p1 = new Vector3f(maxWidth, minY, depth);
            Vector3f p2 = new Vector3f(maxWidth, maxY, depth);
            Vector3f p3 = new Vector3f(minWidth, maxY, depth);
            Direction outward = depth > CENTER ? Direction.NORTH : Direction.SOUTH;
            faces.add(new GeneratedFace(p0, p1, p2, p3, outward));
            faces.add(new GeneratedFace(p3, p2, p1, p0, outward.getOpposite()));
        } else {
            Vector3f p0 = new Vector3f(depth, minY, minWidth);
            Vector3f p1 = new Vector3f(depth, minY, maxWidth);
            Vector3f p2 = new Vector3f(depth, maxY, maxWidth);
            Vector3f p3 = new Vector3f(depth, maxY, minWidth);
            Direction outward = depth > CENTER ? Direction.WEST : Direction.EAST;
            faces.add(new GeneratedFace(p0, p1, p2, p3, outward));
            faces.add(new GeneratedFace(p3, p2, p1, p0, outward.getOpposite()));
        }
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
