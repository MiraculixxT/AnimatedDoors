package de.miraculixx.animated_doors.client.animation;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;

public record GeneratedFace(Vector3fc position0, Vector3fc position1, Vector3fc position2, Vector3fc position3, Direction direction) {
    public BakedQuad bake(BakedQuad template) {
        return new BakedQuad(
            new Vector3f(position0),
            new Vector3f(position1),
            new Vector3f(position2),
            new Vector3f(position3),
            template.packedUV0(),
            template.packedUV1(),
            template.packedUV2(),
            template.packedUV3(),
            direction,
            template.materialInfo()
        );
    }

    public static BakedQuad findTemplate(BlockStateModel model, BlockState state, BlockPos pos) {
        BlockModelRenderState renderState = new BlockModelRenderState();
        List<BlockStateModelPart> parts = renderState.setupModel(new org.joml.Matrix4f(), model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
        model.collectParts(renderState.scratchRandomSource(state.getSeed(pos)), parts);
        return findTemplate(parts);
    }

    public static BakedQuad findTemplate(List<BlockStateModelPart> parts) {
        for (BlockStateModelPart part : parts) {
            for (Direction direction : Direction.values()) {
                List<BakedQuad> quads = part.getQuads(direction);
                if (!quads.isEmpty()) {
                    return quads.getFirst();
                }
            }
        }
        return null;
    }
}
