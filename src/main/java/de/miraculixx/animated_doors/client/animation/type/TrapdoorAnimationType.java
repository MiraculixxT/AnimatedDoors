package de.miraculixx.animated_doors.client.animation.type;

import de.miraculixx.animated_doors.client.animation.AnimationInstance;
import de.miraculixx.animated_doors.client.animation.AnimationMath;
import de.miraculixx.animated_doors.client.animation.RenderInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import org.joml.Matrix4f;

import java.util.List;

public final class TrapdoorAnimationType implements AnimatedBlockType {
    private static final float HALF_THICKNESS = 1.5f / 16.0f;
    private static final float FAR_EDGE_CENTER = 1.0f - HALF_THICKNESS;

    @Override
    public boolean supports(BlockState state) {
        return state.getBlock() instanceof TrapDoorBlock && state.hasProperty(TrapDoorBlock.OPEN);
    }

    @Override
    public boolean hasOpenChanged(BlockState oldState, BlockState newState) {
        return oldState.getBlock().getClass() == newState.getBlock().getClass()
            && oldState.getValue(TrapDoorBlock.OPEN) != newState.getValue(TrapDoorBlock.OPEN);
    }

    @Override
    public boolean isOpen(BlockState state) {
        return state.getValue(TrapDoorBlock.OPEN);
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
        BlockState baseState = animation.oldState.setValue(TrapDoorBlock.OPEN, false);
        Direction facing = baseState.getValue(TrapDoorBlock.FACING);
        Half half = baseState.getValue(TrapDoorBlock.HALF);
        float amount = animation.openAmount(System.nanoTime());
        float angle = amount * ((float) Math.PI / 2.0f);

        Matrix4f transform = half == Half.TOP ? topTransform(facing, angle) : bottomTransform(facing, angle);
        return List.of(new RenderInstruction(baseState, transform));
    }

    private Matrix4f bottomTransform(Direction facing, float angle) {
        return switch (facing) {
            case NORTH -> AnimationMath.rotateX(HALF_THICKNESS, FAR_EDGE_CENTER, angle);
            case SOUTH -> AnimationMath.rotateX(HALF_THICKNESS, HALF_THICKNESS, -angle);
            case WEST -> AnimationMath.rotateZ(FAR_EDGE_CENTER, HALF_THICKNESS, -angle);
            case EAST -> AnimationMath.rotateZ(HALF_THICKNESS, HALF_THICKNESS, angle);
            default -> new Matrix4f();
        };
    }

    private Matrix4f topTransform(Direction facing, float angle) {
        return switch (facing) {
            case NORTH -> AnimationMath.rotateX(FAR_EDGE_CENTER, FAR_EDGE_CENTER, -angle);
            case SOUTH -> AnimationMath.rotateX(FAR_EDGE_CENTER, HALF_THICKNESS, angle);
            case WEST -> AnimationMath.rotateZ(FAR_EDGE_CENTER, FAR_EDGE_CENTER, angle);
            case EAST -> AnimationMath.rotateZ(HALF_THICKNESS, FAR_EDGE_CENTER, -angle);
            default -> new Matrix4f();
        };
    }
}
