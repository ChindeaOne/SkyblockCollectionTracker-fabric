/*
  Class and methods implemented by referencing Skyblocker
 */
//? if 26.2 {
/*
package io.github.chindeaone.collectiontracker.utils.world

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.StagedVertexBuffer
import net.minecraft.client.renderer.rendertype.RenderType

object Renderer {

    private val vertexBuffer: StagedVertexBuffer = StagedVertexBuffer({ "SkyblockCollectionTracker Vertex Buffer" }, RenderType.SMALL_BUFFER_SIZE)
    private val previousPipeline: RenderPipeline? = null
    private val previousTextureSetup: TextureSetup? = null
    private val previousAlphaMultiplier: Float? = null
    private val previousInstanceCount: Int? = null
    private val previousUniform: UniformBinding? = null
    private val DRAWS: ArrayList<Draw> = ArrayList()
    private var previousDraw: StagedVertexBuffer.Draw? = null

    fun getBuffer(renderPipeline: RenderPipeline): VertexConsumer {
        return getBuffer(renderPipeline, TextureSetup.noTexture(), 1f, 1, null)
    }

    fun getBuffer(renderPipeline: RenderPipeline, textureSetup: TextureSetup): VertexConsumer {
        return getBuffer(renderPipeline, textureSetup, 1f, 1, null)
    }

    fun getBuffer(renderPipeline: RenderPipeline, textureSetup: TextureSetup, alphaMultiplier: Float, instanceCount: Int, uniformBinding: UniformBinding?): VertexConsumer {
        if (previousDraw == null || renderPipeline != previousPipeline || textureSetup != previousTextureSetup || alphaMultiplier != previousAlphaMultiplier || instanceCount != previousInstanceCount || !uniformBinding?.equals(previousUniform)!!) {
            previousDraw = renderPipeline.getVertexFormatBinding(0)?.let { vertexBuffer.appendDraw(it, renderPipeline.primitiveTopology) }
            DRAWS.add(Draw(previousDraw, renderPipeline, textureSetup, alphaMultiplier, instanceCount, uniformBinding))
        }
        return vertexBuffer.getVertexBuilder(previousDraw!!)
    }

    @JvmRecord
    private data class Draw(
        val draw: StagedVertexBuffer.Draw?,
        val pipeline: RenderPipeline?,
        val textureSetup: TextureSetup?,
        val alphaMultiplier: Float,
        val instanceCount: Int,
        val uniform: UniformBinding?
    )

    @JvmRecord
    data class UniformBinding(val name: String, val buffer: GpuBuffer?)
}
*///?}