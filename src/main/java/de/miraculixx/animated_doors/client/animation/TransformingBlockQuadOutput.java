package de.miraculixx.animated_doors.client.animation;

import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.function.Predicate;

public final class TransformingBlockQuadOutput implements BlockQuadOutput {
    private final BlockQuadOutput delegate;
    private final Matrix4fc transform;
    private final Predicate<BakedQuad> filter;

    public TransformingBlockQuadOutput(BlockQuadOutput delegate, Matrix4fc transform, Predicate<BakedQuad> filter) {
        this.delegate = delegate;
        this.transform = transform;
        this.filter = filter;
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance quadInstance) {
        if (!filter.test(quad)) {
            return;
        }

        BakedQuad transformed = new BakedQuad(
            transform(quad, 0),
            transform(quad, 1),
            transform(quad, 2),
            transform(quad, 3),
            quad.packedUV0(),
            quad.packedUV1(),
            quad.packedUV2(),
            quad.packedUV3(),
            quad.direction(),
            quad.materialInfo()
        );
        delegate.put(x, y, z, transformed, quadInstance);
    }

    private Vector3f transform(BakedQuad quad, int vertex) {
        return new Vector3f(quad.position(vertex)).mulPosition(transform);
    }
}
