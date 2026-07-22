package io.github.chindeaone.collectiontracker.utils.world

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.CompareOp
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import java.util.Optional

object CustomPipelines {
    lateinit var LINE_THROUGH_WALLS: RenderPipeline
    lateinit var HIGHLIGHT: RenderPipeline
    lateinit var CHROMA_TEXT: RenderPipeline

    fun register() {
        LINE_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "pipeline/line_through_walls"))
                .withDepthStencilState(Optional.empty())
                .build()
        )
        HIGHLIGHT = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "pipeline/highlight"))
                .withDepthStencilState(Optional.of(DepthStencilState(
                    CompareOp./*? if 26.2 {*/ /*GREATER_THAN_OR_EQUAL *//*?} else {*/ LESS_THAN_OR_EQUAL /*?}*/,
                    false)))
                .build()
        )
        CHROMA_TEXT = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "pipeline/chroma_text"))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).apply {
                    withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                    withVertexShader(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "core/chroma_text"))
                    withFragmentShader(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "core/chroma_text"))
                    withSampler("Sampler0")
                    withUniform("SctChromaUniforms", UniformType.UNIFORM_BUFFER)
                }
                .build()
        )
    }
}