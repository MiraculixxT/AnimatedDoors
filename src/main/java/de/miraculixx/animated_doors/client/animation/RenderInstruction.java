package de.miraculixx.animated_doors.client.animation;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4fc;

import java.util.List;
import java.util.function.Predicate;

public record RenderInstruction(BlockState state, Matrix4fc transform, Predicate<BakedQuad> filter, List<GeneratedFace> generatedFaces) {
    public RenderInstruction(BlockState state, Matrix4fc transform) {
        this(state, transform, quad -> true);
    }

    public RenderInstruction(BlockState state, Matrix4fc transform, Predicate<BakedQuad> filter) {
        this(state, transform, filter, List.of());
    }
}
