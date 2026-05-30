package de.miraculixx.animated_doors.client;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4fc;

import java.util.function.Predicate;

public record RenderInstruction(BlockState state, Matrix4fc transform, Predicate<BakedQuad> filter) {
    public RenderInstruction(BlockState state, Matrix4fc transform) {
        this(state, transform, quad -> true);
    }
}
