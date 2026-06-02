package de.miraculixx.animated_doors.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
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
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
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
        private final BlockModelLighter lighter = new BlockModelLighter();
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

            if (usesVanillaSmoothLighting(state)) {
                submitSmooth(minecraft.level, pos, state, transform, quads, renderType);
                return;
            }

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

        private void submitSmooth(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            Matrix4fc transform,
            List<BakedQuad> quads,
            RenderType renderType
        ) {
            List<SmoothQuad> smoothQuads = new ArrayList<>(quads.size());
            for (BakedQuad quad : quads) {
                smoothQuads.add(prepareSmoothQuad(level, pos, state, quad, transform));
            }

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
                (pose, vertexConsumer) -> renderSmoothQuads(vertexConsumer, pose, smoothQuads)
            );
            poseStack.popPose();
        }

        private SmoothQuad prepareSmoothQuad(BlockAndTintGetter level, BlockPos pos, BlockState state, BakedQuad quad, Matrix4fc transform) {
            Vector3f normal = transformedNormal(quad, transform);
            float xWeight = normal.x() * normal.x();
            float yWeight = normal.y() * normal.y();
            float zWeight = normal.z() * normal.z();
            float totalWeight = xWeight + yWeight + zWeight;
            if (totalWeight <= 0.0f) {
                yWeight = 1.0f;
                totalWeight = 1.0f;
            }
            xWeight /= totalWeight;
            yWeight /= totalWeight;
            zWeight /= totalWeight;

            QuadInstance xLighting = prepareDirectionalSmoothQuad(level, pos, state, quad, transform, normal.x() >= 0.0f ? Direction.EAST : Direction.WEST);
            QuadInstance yLighting = prepareDirectionalSmoothQuad(level, pos, state, quad, transform, normal.y() >= 0.0f ? Direction.UP : Direction.DOWN);
            QuadInstance zLighting = prepareDirectionalSmoothQuad(level, pos, state, quad, transform, normal.z() >= 0.0f ? Direction.SOUTH : Direction.NORTH);

            int[] colors = new int[BakedQuad.VERTEX_COUNT];
            int[] lightCoords = new int[BakedQuad.VERTEX_COUNT];
            for (int vertex = 0; vertex < BakedQuad.VERTEX_COUNT; vertex++) {
                colors[vertex] = weightedColor(
                    xLighting.getColor(vertex),
                    yLighting.getColor(vertex),
                    zLighting.getColor(vertex),
                    xWeight,
                    yWeight,
                    zWeight
                );
                lightCoords[vertex] = LightCoordsUtil.smoothWeightedBlend(
                    xLighting.getLightCoords(vertex),
                    yLighting.getLightCoords(vertex),
                    zLighting.getLightCoords(vertex),
                    zLighting.getLightCoords(vertex),
                    xWeight,
                    yWeight,
                    zWeight,
                    0.0f
                );
            }
            return new SmoothQuad(quad, colors, lightCoords);
        }

        private QuadInstance prepareDirectionalSmoothQuad(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            BakedQuad quad,
            Matrix4fc transform,
            Direction direction
        ) {
            QuadInstance quadInstance = new QuadInstance();
            quadInstance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
            lighter.prepareQuadAmbientOcclusion(level, state, pos, transformQuad(quad, transform, direction), quadInstance);
            return quadInstance;
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

        private static void renderSmoothQuads(VertexConsumer vertexConsumer, PoseStack.Pose pose, List<SmoothQuad> quads) {
            QuadInstance quadInstance = new QuadInstance();
            for (SmoothQuad quad : quads) {
                quadInstance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
                for (int vertex = 0; vertex < BakedQuad.VERTEX_COUNT; vertex++) {
                    quadInstance.setColor(vertex, quad.colors()[vertex]);
                    quadInstance.setLightCoords(vertex, quad.lightCoords()[vertex]);
                }
                vertexConsumer.putBakedQuad(pose, quad.quad(), quadInstance);
            }
        }

        private static float shade(Vector3f normal, CardinalLighting cardinalLighting) {
            float x = normal.x() * normal.x();
            float y = normal.y() * normal.y();
            float z = normal.z() * normal.z();
            float yShade = normal.y() > 0.0f ? cardinalLighting.byFace(Direction.UP) : cardinalLighting.byFace(Direction.DOWN);
            return x * cardinalLighting.byFace(Direction.EAST) + y * yShade + z * cardinalLighting.byFace(Direction.NORTH);
        }

        private static boolean usesVanillaSmoothLighting(BlockState state) {
            return state.getBlock() instanceof TrapDoorBlock || state.getBlock() instanceof FenceGateBlock;
        }

        private static BakedQuad transformQuad(BakedQuad quad, Matrix4fc transform, Direction direction) {
            return new BakedQuad(
                transformPosition(quad, 0, transform),
                transformPosition(quad, 1, transform),
                transformPosition(quad, 2, transform),
                transformPosition(quad, 3, transform),
                quad.packedUV0(),
                quad.packedUV1(),
                quad.packedUV2(),
                quad.packedUV3(),
                direction,
                quad.materialInfo()
            );
        }

        private static Vector3f transformPosition(BakedQuad quad, int vertex, Matrix4fc transform) {
            return new Vector3f(quad.position(vertex)).mulPosition(transform);
        }

        private static Vector3f transformedNormal(BakedQuad quad, Matrix4fc transform) {
            Vector3f normal = new Vector3f(quad.direction().getUnitVec3f()).mulDirection(transform);
            if (normal.lengthSquared() == 0.0f) {
                return new Vector3f(Direction.UP.getUnitVec3f());
            }
            return normal.normalize();
        }

        private static int weightedColor(int xColor, int yColor, int zColor, float xWeight, float yWeight, float zWeight) {
            int alpha = weightedChannel(ARGB.alpha(xColor), ARGB.alpha(yColor), ARGB.alpha(zColor), xWeight, yWeight, zWeight);
            int red = weightedChannel(ARGB.red(xColor), ARGB.red(yColor), ARGB.red(zColor), xWeight, yWeight, zWeight);
            int green = weightedChannel(ARGB.green(xColor), ARGB.green(yColor), ARGB.green(zColor), xWeight, yWeight, zWeight);
            int blue = weightedChannel(ARGB.blue(xColor), ARGB.blue(yColor), ARGB.blue(zColor), xWeight, yWeight, zWeight);
            return ARGB.color(alpha, red, green, blue);
        }

        private static int weightedChannel(int x, int y, int z, float xWeight, float yWeight, float zWeight) {
            return Math.clamp(Math.round(x * xWeight + y * yWeight + z * zWeight), 0, 255);
        }
    }

    private record SmoothQuad(BakedQuad quad, int[] colors, int[] lightCoords) {
    }
}
