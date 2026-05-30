package de.miraculixx.animated_doors.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface AnimatedBlockType {
    boolean supports(BlockState state);

    boolean hasOpenChanged(BlockState oldState, BlockState newState);

    boolean isOpen(BlockState state);

    BlockPos normalize(BlockGetter level, BlockPos pos, BlockState state);

    List<BlockPos> affectedPositions(BlockGetter level, BlockPos normalizedPos, BlockState state);

    List<RenderInstruction> renderInstructions(AnimationInstance animation, BlockPos pos);
}
