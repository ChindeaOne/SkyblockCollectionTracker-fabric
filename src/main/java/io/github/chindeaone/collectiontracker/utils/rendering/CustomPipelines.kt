package io.github.chindeaone.collectiontracker.utils.rendering

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.CompareOp
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
//? if 26.2 {
/*import net.minecraft.client.renderer.BindGroupLayouts
import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.pipeline.BindGroupLayout
*///?} else {
import com.mojang.blaze3d.vertex.VertexFormat
//?}
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.Identifier
import java.util.Optional

object CustomPipelines {
    lateinit var LINE_THROUGH_WALLS: RenderPipeline
    lateinit var HIGHLIGHT: RenderPipeline
    lateinit var CHROMA_TEXT: RenderPipeline
    lateinit var PREFIX_GRADIENT_TEXT: RenderPipeline

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
                .withDepthStencilState(
                    Optional.of(
                        DepthStencilState(
                            CompareOp./*? if 26.2 {*/ /*GREATER_THAN_OR_EQUAL *//*?} else {*/ LESS_THAN_OR_EQUAL /*?}*/,
                            false
                        )
                    ))
                .build()
        )
        CHROMA_TEXT = RenderPipelines.register(
            RenderPipeline.builder(/*? if 26.1 {*/ RenderPipelines.MATRICES_PROJECTION_SNIPPET /*?}*/)
                .withLocation(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "pipeline/chroma_text"))
                //? if 26.2 {
                /*.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).apply {
                    withPrimitiveTopology(PrimitiveTopology.QUADS)
                    withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                    withVertexShader(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "core/chroma_text"))
                    withFragmentShader(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "core/chroma_text"))
                    withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                    withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    withBindGroupLayout(BindGroupLayout.builder()
                        .withUniform("SctChromaUniforms", UniformType.UNIFORM_BUFFER)
                        .build()
                    )
                }
                *///?} else {
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).apply {
                    withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                    withVertexShader(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "core/chroma_text"))
                    withFragmentShader(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "core/chroma_text"))
                    withSampler("Sampler0")
                    withUniform("SctChromaUniforms", UniformType.UNIFORM_BUFFER)
                }
                //?}
                .build()
        )
        PREFIX_GRADIENT_TEXT = RenderPipelines.register(
            RenderPipeline.builder(/*? if 26.1 {*/ RenderPipelines.MATRICES_PROJECTION_SNIPPET /*?}*/)
                .withLocation(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "pipeline/prefix_gradient_text"))
                //? if 26.2 {
                /*.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).apply {
                    withPrimitiveTopology(PrimitiveTopology.QUADS)
                    withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                    withVertexShader(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "core/chroma_text"))
                    withFragmentShader(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "core/chroma_text"))
                    withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                    withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    withBindGroupLayout(BindGroupLayout.builder()
                        .withUniform("SctChromaUniforms", UniformType.UNIFORM_BUFFER)
                        .build()
                    )
                }
                *///?} else {
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).apply {
                    withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                    withVertexShader(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "core/chroma_text"))
                    withFragmentShader(Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "core/chroma_text"))
                    withSampler("Sampler0")
                    withUniform("SctChromaUniforms", UniformType.UNIFORM_BUFFER)
                }
                //?}
                .build()
        )

    }
}