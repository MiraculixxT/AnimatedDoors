package de.miraculixx.animated_doors.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class AnimatedDoorRenderer {
    private AnimatedDoorRenderer() {
    }

    public static void render(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 cameraPos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        RenderContext renderContext = new RenderContext(
            minecraft.getModelManager().getBlockStateModelSet(),
            poseStack,
            submitNodeCollector,
            cameraPos
        );

        for (AnimationInstance animation : AnimationManager.activeAnimations()) {
            for (BlockPos pos : animation.affectedPositions) {
                for (RenderInstruction instruction : animation.type.renderInstructions(animation, pos)) {
                    renderContext.submit(pos, instruction.state(), instruction.transform(), instruction.filter(), instruction.generatedFaces());
                }
            }
        }
    }

    private static final class RenderContext {
        private static final Direction[] DIRECTIONS = Direction.values();

        private final BlockStateModelSet modelSet;
        private final PoseStack poseStack;
        private final SubmitNodeCollector submitNodeCollector;
        private final Vec3 cameraPos;

        private RenderContext(BlockStateModelSet modelSet, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 cameraPos) {
            this.modelSet = modelSet;
            this.poseStack = poseStack;
            this.submitNodeCollector = submitNodeCollector;
            this.cameraPos = cameraPos;
        }

        private void submit(BlockPos pos, BlockState state, Matrix4fc transform, Predicate<BakedQuad> quadFilter, List<GeneratedFace> generatedFaces) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }

            BlockStateModel model = modelSet.get(state);
            List<BlockStateModelPart> parts = collectParts(model, state, pos);
            List<BakedQuad> quads = collectQuads(parts, quadFilter);
            if (!generatedFaces.isEmpty()) {
                BakedQuad template = GeneratedFace.findTemplate(parts);
                if (template != null) {
                    for (GeneratedFace face : generatedFaces) {
                        quads.add(face.bake(template));
                    }
                }
            }
            if (quads.isEmpty()) {
                return;
            }

            int lightCoords = LevelRenderer.getLightCoords(minecraft.level, pos);
            CardinalLighting cardinalLighting = minecraft.level.cardinalLighting();
            RenderType renderType = model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT)
                ? RenderTypes.translucentMovingBlock()
                : RenderTypes.cutoutMovingBlock();

            poseStack.pushPose();
            poseStack.translate(
                pos.getX() - cameraPos.x,
                pos.getY() - cameraPos.y,
                pos.getZ() - cameraPos.z
            );
            poseStack.mulPose(transform);
            submitNodeCollector.submitCustomGeometry(
                poseStack,
                renderType,
                (pose, vertexConsumer) -> renderQuads(vertexConsumer, pose, quads, lightCoords, cardinalLighting)
            );
            poseStack.popPose();
        }

        private static List<BlockStateModelPart> collectParts(BlockStateModel model, BlockState state, BlockPos pos) {
            BlockModelRenderState renderState = new BlockModelRenderState();
            List<BlockStateModelPart> parts = renderState.setupModel(new Matrix4f(), model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
            model.collectParts(renderState.scratchRandomSource(state.getSeed(pos)), parts);
            return parts;
        }

        private static List<BakedQuad> collectQuads(List<BlockStateModelPart> parts, Predicate<BakedQuad> quadFilter) {
            List<BakedQuad> quads = new ArrayList<>();
            for (BlockStateModelPart part : parts) {
                for (Direction direction : DIRECTIONS) {
                    collectQuads(part.getQuads(direction), quadFilter, quads);
                }
                collectQuads(part.getQuads(null), quadFilter, quads);
            }
            return quads;
        }

        private static void collectQuads(List<BakedQuad> source, Predicate<BakedQuad> quadFilter, List<BakedQuad> quads) {
            for (BakedQuad quad : source) {
                if (quadFilter.test(quad)) {
                    quads.add(quad);
                }
            }
        }

        private static void renderQuads(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
            List<BakedQuad> quads,
            int lightCoords,
            CardinalLighting cardinalLighting
        ) {
            QuadInstance quadInstance = new QuadInstance();
            quadInstance.setLightCoords(lightCoords);
            quadInstance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
            Vector3f normal = new Vector3f();

            for (BakedQuad quad : quads) {
                pose.transformNormal(quad.direction().getUnitVec3f(), normal).normalize();
                float shade = shade(normal, cardinalLighting);
                quadInstance.setColor(ARGB.colorFromFloat(1.0f, shade, shade, shade));
                vertexConsumer.putBakedQuad(pose, quad, quadInstance);
            }
        }

        private static float shade(Vector3f normal, CardinalLighting cardinalLighting) {
            float x = normal.x() * normal.x();
            float y = normal.y() * normal.y();
            float z = normal.z() * normal.z();
            float yShade = normal.y() > 0.0f ? cardinalLighting.byFace(Direction.UP) : cardinalLighting.byFace(Direction.DOWN);
            return x * cardinalLighting.byFace(Direction.EAST) + y * yShade + z * cardinalLighting.byFace(Direction.NORTH);
        }
    }
}
