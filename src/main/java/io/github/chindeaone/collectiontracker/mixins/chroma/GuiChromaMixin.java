package io.github.chindeaone.collectiontracker.mixins.chroma;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;
import io.github.chindeaone.collectiontracker.utils.rendering.ChromaText;
import io.github.chindeaone.collectiontracker.utils.rendering.ChromaRenderer;
import io.github.chindeaone.collectiontracker.utils.world.CustomPipelines;
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
                if (ChromaText.isChromaGlyph(glyph)) {
                    return CustomPipelines.CHROMA_TEXT;
                }
            }
        }

        return original.call(state);
    }

    @Inject(method = "executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;II)V", at = @At("HEAD"))
    public void prepareChromaUniform(
            Supplier<String> label,
            RenderTarget mainRenderTarget,
            GpuBufferSlice fogBuffer,
            GpuBufferSlice dynamicTransforms,
            GpuBuffer indexBuffer,
            IndexType indexType,
            int startIndex, int endIndex, CallbackInfo ci
    ) {
        ChromaRenderer.prepareUniform();
    }

    @Inject(method = "executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;II)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", ordinal = 1))
    private void bindChromaUniform(
            Supplier<String> label,
            RenderTarget mainRenderTarget,
            GpuBufferSlice fogBuffer,
            GpuBufferSlice dynamicTransforms,
            GpuBuffer indexBuffer,
            IndexType indexType,
            int startIndex, int endIndex, CallbackInfo ci,
            @Local(name = "renderPass") RenderPass renderPass
    ) {
        ChromaRenderer.bindUniform(renderPass);
    }
}
