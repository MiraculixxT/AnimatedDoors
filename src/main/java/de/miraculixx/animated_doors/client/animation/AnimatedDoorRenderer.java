package de.miraculixx.animated_doors.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

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
            BlockModelRenderState renderState = new BlockModelRenderState();
            List<BlockStateModelPart> parts = renderState.setupModel(transform, model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
            model.collectParts(renderState.scratchRandomSource(state.getSeed(pos)), parts);

            List<BlockStateModelPart> filtered = new ArrayList<>(parts.size());
            for (BlockStateModelPart part : parts) {
                filtered.add(new FilteringPart(part, quadFilter));
            }
            if (!generatedFaces.isEmpty()) {
                BakedQuad template = GeneratedFace.findTemplate(parts);
                if (template != null) {
                    filtered.add(new GeneratedPart(generatedFaces, template, model.particleMaterial()));
                }
            }
            parts.clear();
            parts.addAll(filtered);

            poseStack.pushPose();
            poseStack.translate(
                pos.getX() - cameraPos.x,
                pos.getY() - cameraPos.y,
                pos.getZ() - cameraPos.z
            );
            renderState.submit(poseStack, submitNodeCollector, LevelRenderer.getLightCoords(minecraft.level, pos), OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    private static final class GeneratedPart implements BlockStateModelPart {
        private final List<GeneratedFace> faces;
        private final BakedQuad template;
        private final Material.Baked particleMaterial;

        private GeneratedPart(List<GeneratedFace> faces, BakedQuad template, Material.Baked particleMaterial) {
            this.faces = faces;
            this.template = template;
            this.particleMaterial = particleMaterial;
        }

        @Override
        public List<BakedQuad> getQuads(Direction direction) {
            List<BakedQuad> quads = new ArrayList<>(faces.size());
            for (GeneratedFace face : faces) {
                if (direction == null || face.direction() == direction) {
                    quads.add(face.bake(template));
                }
            }
            return quads;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return false;
        }

        @Override
        public Material.Baked particleMaterial() {
            return particleMaterial;
        }

        @Override
        public int materialFlags() {
            return template.materialInfo().flags();
        }
    }

    private static final class FilteringPart implements BlockStateModelPart {
        private final BlockStateModelPart delegate;
        private final Predicate<BakedQuad> filter;

        private FilteringPart(BlockStateModelPart delegate, Predicate<BakedQuad> filter) {
            this.delegate = delegate;
            this.filter = filter;
        }

        @Override
        public List<BakedQuad> getQuads(Direction direction) {
            List<BakedQuad> original = delegate.getQuads(direction);
            if (original.isEmpty()) {
                return original;
            }

            List<BakedQuad> filtered = new ArrayList<>(original.size());
            for (BakedQuad quad : original) {
                if (filter.test(quad)) {
                    filtered.add(quad);
                }
            }
            return filtered;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return delegate.useAmbientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return delegate.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return delegate.materialFlags();
        }
    }
}
