/*
  Class and methods implemented by referencing Skyblocker
 */
package io.github.chindeaone.collectiontracker.utils.render

import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.rendertype.RenderType
import org.joml.Vector4f
import java.util.OptionalDouble
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import net.minecraft.client.renderer.StagedVertexBuffer
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import java.util.Optional

object WorldRenderer {

    private val vertexBuffer = StagedVertexBuffer({ "SkyblockCollectionTracker Renderer Vertex Buffer" }, RenderType.SMALL_BUFFER_SIZE)

    private var previousPipeline: RenderPipeline? = null
    private var previousTextureSetup: TextureSetup? = null
    private var previousAlphaMultiplier: Float = 1f
    private var previousInstanceCount: Int = 1
    private var previousUniform: UniformBinding? = null
    private var previousDraw: StagedVertexBuffer.Draw? = null

    private val draws = ArrayList<Draw>()

    fun getBuffer(pipeline: RenderPipeline): VertexConsumer {
        return getBuffer(
            pipeline = pipeline,
            textureSetup = TextureSetup.noTexture(),
            alphaMultiplier = 1f,
            instanceCount = 1,
            uniform = null
        )
    }

    fun getBuffer(pipeline: RenderPipeline, textureSetup: TextureSetup): VertexConsumer {
        return getBuffer(
            pipeline = pipeline,
            textureSetup = textureSetup,
            alphaMultiplier = 1f,
            instanceCount = 1,
            uniform = null
        )
    }

    fun getBuffer(
        pipeline: RenderPipeline,
        textureSetup: TextureSetup,
        alphaMultiplier: Float,
        instanceCount: Int,
        uniform: UniformBinding?
    ): VertexConsumer {
        val needsNewDraw = previousDraw == null ||
                pipeline != previousPipeline ||
                textureSetup != previousTextureSetup ||
                alphaMultiplier != previousAlphaMultiplier ||
                instanceCount != previousInstanceCount ||
                uniform != previousUniform

        if (needsNewDraw) {
            val vertexFormatBinding = pipeline.getVertexFormatBinding(0)
                ?: error("Pipeline has no vertex format binding at index 0: $pipeline")

            val newDraw = vertexBuffer.appendDraw(vertexFormatBinding, pipeline.primitiveTopology)

            previousDraw = newDraw
            previousPipeline = pipeline
            previousTextureSetup = textureSetup
            previousAlphaMultiplier = alphaMultiplier
            previousInstanceCount = instanceCount
            previousUniform = uniform

            draws.add(
                Draw(
                    draw = newDraw,
                    pipeline = pipeline,
                    textureSetup = textureSetup,
                    alphaMultiplier = alphaMultiplier,
                    instanceCount = instanceCount,
                    uniform = uniform
                )
            )
        }

        return vertexBuffer.getVertexBuilder(
            requireNotNull(previousDraw) { "Expected previousDraw to be set before getting vertex builder" }
        )
    }

    fun prepare() {
        previousDraw = null
        previousPipeline = null
        previousTextureSetup = null
        previousAlphaMultiplier = 1f
        previousInstanceCount = 1
        previousUniform = null
    }

    fun executeDraws() {
        if (draws.isEmpty()) {
            prepare()
            return
        }

        try {
            vertexBuffer.upload()
            dispatchDraws()
        } finally {
            vertexBuffer.endDraw()
            vertexBuffer.endFrame()
            draws.clear()
            prepare()
        }
    }

    private fun dispatchDraws() {
        applyViewOffsetZLayering()

        try {
            val mainRenderTarget = MinecraftUtils.gameRenderer.mainRenderTarget()

            val colorTexture = mainRenderTarget.getColorTextureView() ?: error("Main render target color texture view is null")
            val depthTexture = mainRenderTarget.getDepthTextureView() ?: error("Main render target depth texture view is null")

            RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                    { "SkyblockCollectionTracker Level Rendering" },
                    colorTexture,
                    Optional.empty(),
                    depthTexture,
                    OptionalDouble.empty()
                ).use { renderPass ->
                    RenderSystem.bindDefaultUniforms(renderPass)

                    for (draw in draws) {
                        draw(draw, renderPass)
                    }
                }
        } finally {
            unapplyViewOffsetZLayering()
        }
    }

    private fun draw(draw: Draw, renderPass: RenderPass) {
        val executeInfo = vertexBuffer.getExecuteInfo(draw.draw) ?: return

        if (executeInfo.indexCount() <= 0) {
            return
        }

        //? if 26.3 {
        /*renderPass.setPipeline(
            RenderSystem.getCompiledPipeline(draw.pipeline)
        )
        *///? } else {
        renderPass.setPipeline(draw.pipeline)
        //? }

        renderPass.setUniform(
            "DynamicTransforms",
            setupDynamicTransforms(draw.alphaMultiplier)
        )

        draw.uniform?.let { uniform ->
            renderPass.setUniform(uniform.name, uniform.buffer)
        }

        draw.textureSetup.texure0()?.let { texture ->
            //? if 26.3 {
            //renderPass.setUniform("Sampler0", texture, draw.textureSetup.sampler0())
            //? } else {
            renderPass.bindTexture("Sampler0", texture, draw.textureSetup.sampler0())
            //? }
        }

        draw.textureSetup.texure1()?.let { texture ->
            //? if 26.3 {
            //renderPass.setUniform("Sampler1", texture, draw.textureSetup.sampler1())
            //? } else {
            renderPass.bindTexture("Sampler1", texture, draw.textureSetup.sampler1())
            //? }
        }

        draw.textureSetup.texure2()?.let { texture ->
            //? if 26.3 {
            //renderPass.setUniform("Sampler2", texture, draw.textureSetup.sampler2())
            //? } else {
            renderPass.bindTexture("Sampler2", texture, draw.textureSetup.sampler2())
            //? }
        }

        renderPass.setVertexBuffer(0, executeInfo.vertexBuffer().slice())
        renderPass.setIndexBuffer(executeInfo.indexBuffer(), executeInfo.indexType())

        renderPass.drawIndexed(
            executeInfo.indexCount(),
            draw.instanceCount,
            executeInfo.firstIndex(),
            executeInfo.baseVertex(),
            0
        )
    }

    private fun setupDynamicTransforms(alphaMultiplier: Float): GpuBufferSlice {
        return RenderSystem.getDynamicUniforms()
            .writeTransform(
                RenderSystem.getModelViewMatrixCopy(),
                Vector4f(1f, 1f, 1f, alphaMultiplier)
            )
    }

    private fun applyViewOffsetZLayering() {
        val modelViewStack = RenderSystem.getModelViewStack()

        modelViewStack.pushMatrix()
        RenderSystem.getProjectionType().applyLayeringTransform(modelViewStack, 1f)
    }

    private fun unapplyViewOffsetZLayering() {
        RenderSystem.getModelViewStack().popMatrix()
    }

    @JvmStatic
    fun close() {
        vertexBuffer.close()
    }

    private data class Draw(
        val draw: StagedVertexBuffer.Draw,
        val pipeline: RenderPipeline,
        val textureSetup: TextureSetup,
        val alphaMultiplier: Float,
        val instanceCount: Int,
        val uniform: UniformBinding?
    )

    data class UniformBinding(
        val name: String,
        val buffer: GpuBuffer
    )
}