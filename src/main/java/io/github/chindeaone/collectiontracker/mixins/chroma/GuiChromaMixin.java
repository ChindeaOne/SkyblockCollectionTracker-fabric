package io.github.chindeaone.collectiontracker.mixins.chroma;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
//? if 26.1 {
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;
//?}
import io.github.chindeaone.collectiontracker.utils.rendering.ChromaText;
import io.github.chindeaone.collectiontracker.utils.rendering.ChromaRenderer;
import io.github.chindeaone.collectiontracker.utils.rendering.CustomPipelines;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph.GlyphInstance;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GlyphRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(GuiRenderer.class)
public class GuiChromaMixin {

    @WrapOperation(
            method = "addElementToMesh",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/state/gui/GuiElementRenderState;pipeline()Lcom/mojang/blaze3d/pipeline/RenderPipeline;"
            )
    )
    private RenderPipeline replacePipeline(GuiElementRenderState state, Operation<RenderPipeline> original) {
        if (state instanceof GlyphRenderState glyphState) {
            if (glyphState.renderable() instanceof GlyphInstance glyph) {
                if (ChromaText.isPrefixGradientGlyph(glyph)) {
                    return CustomPipelines.PREFIX_GRADIENT_TEXT;
                }

                if (ChromaText.isChromaGlyph(glyph)) {
                    return CustomPipelines.CHROMA_TEXT;
                }
            }
        }

        return original.call(state);
    }

    @Inject(
            method = "executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;II)V",
            at = @At("HEAD")
    )
    public void prepareChromaUniforms(
            Supplier<String> label,
            RenderTarget mainRenderTarget,
            //? if 26.1
            GpuBufferSlice fogBuffer,
            GpuBufferSlice dynamicTransforms,
            //? if 26.1
            GpuBuffer indexBuffer,
            //? if 26.1
            IndexType indexType,
            int startIndex, int endIndex, CallbackInfo ci
    ) {
        ChromaRenderer.prepareUniforms();
    }

    @Inject(
            method = "executeDraw",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderPass;setPipeline(Lcom/mojang/blaze3d/pipeline/RenderPipeline;)V"
            )
    )
    private void bindChromaUniformPerDraw(
            GuiRenderer.Draw draw,
            RenderPass renderPass,
            //? if 26.1
            GpuBuffer indexBuffer,
            //? if 26.1
            IndexType indexType,
            CallbackInfo ci
    ) {
        RenderPipeline pipeline = draw.pipeline();

        if (pipeline == CustomPipelines.CHROMA_TEXT) {
            ChromaRenderer.bindNormalChroma(renderPass);
        } else if (pipeline == CustomPipelines.PREFIX_GRADIENT_TEXT) {
            ChromaRenderer.bindPrefixGradient(renderPass);
        }
    }
}
