/*
  Class and methods implemented by referencing Skyblocker
 */
package io.github.chindeaone.collectiontracker.utils.world

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.pipeline.RenderPipeline
/*? if 26.2 {*/
/*import com.mojang.blaze3d.systems.RenderPass
*//*?}*/
import com.mojang.blaze3d.systems.RenderSystem
/*? if 26.2 {*/
/*import net.minecraft.client.renderer.StagedVertexBuffer
import org.joml.Matrix4fStack
import java.util.Optional
*//*?} else {*/

import com.mojang.blaze3d.textures.GpuTextureView
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.MeshData
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.MappableRingBuffer
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.util.OptionalInt
/*?}*/
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.rendertype.RenderType
import org.joml.Vector4f
import java.util.OptionalDouble

object Renderer {

    private val client: Minecraft = Minecraft.getInstance()

    /*? if 26.2 {*/
    /*private val vertexBuffer = StagedVertexBuffer({ "SkyblockCollectionTracker Renderer Vertex Buffer" }, RenderType.SMALL_BUFFER_SIZE)

    private var previousPipeline: RenderPipeline? = null
    private var previousTextureSetup: TextureSetup? = null
    private var previousAlphaMultiplier: Float = 1f
    private var previousInstanceCount: Int = 1
    private var previousUniform: UniformBinding? = null
    private var previousDraw: StagedVertexBuffer.Draw? = null
    *//*?} else {*/
    
    private val generalAllocator = ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE)
    private val allocators = HashMap<BatchKey, ByteBufferBuilder>()

    private val modelOffset = Vector3f()
    private val textureMatrix = Matrix4f()

    private val batchedDraws = LinkedHashMap<BatchKey, BatchedDraw>()
    private val vertexBuffers = HashMap<VertexFormat, MappableRingBuffer>()

    private val preparedDraws = ArrayList<PreparedDraw>()
    /*?}*/

    private val draws = ArrayList<Draw>()

    fun getBuffer(pipeline: RenderPipeline): VertexConsumer {
        return getBuffer(
            pipeline = pipeline,
            textureSetup = TextureSetup.noTexture(),
            alphaMultiplier = 1f/*? if 26.2 {*//*,
            instanceCount = 1,
            uniform = null*//*?}*/
        )
    }

    fun getBuffer(pipeline: RenderPipeline, textureSetup: TextureSetup): VertexConsumer {
        return getBuffer(
            pipeline = pipeline,
            textureSetup = textureSetup,
            alphaMultiplier = 1f/*? if 26.2 {*//*,
            instanceCount = 1,
            uniform = null*//*?}*/
        )
    }

    fun getBuffer(
        pipeline: RenderPipeline,
        textureSetup: TextureSetup,
        alphaMultiplier: Float/*? if 26.2 {*//*,
        instanceCount: Int,
        uniform: UniformBinding?*//*?}*/
    ): VertexConsumer {
        /*? if 26.2 {*/
        /*val needsNewDraw = previousDraw == null ||
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
        *//*?} else {*/
        
        val key = BatchKey(pipeline, textureSetup, alphaMultiplier)

        val existing = batchedDraws[key]
        if (existing != null) {
            return existing.bufferBuilder
        }

        val allocator = allocators.getOrPut(key) {
            ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE)
        }

        val bufferBuilder = BufferBuilder(allocator, pipeline.vertexFormatMode, pipeline.vertexFormat)

        batchedDraws[key] = BatchedDraw(bufferBuilder, pipeline, textureSetup, alphaMultiplier)

        return bufferBuilder
        /*?}*/
    }

    /*? if 26.2 {*/
    /*fun prepare() {
        previousDraw = null
        previousPipeline = null
        previousTextureSetup = null
        previousAlphaMultiplier = 1f
        previousInstanceCount = 1
        previousUniform = null
    }
    *//*?}*/

    fun executeDraws() {
        /*? if 26.2 {*/
        /*if (draws.isEmpty()) {
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
        *//*?} else {*/
        
        if (batchedDraws.isEmpty()) {
            return
        }

        try {
            endBatches()

            if (preparedDraws.isEmpty()) {
                return
            }

            setupDraws()

            for (draw in draws) {
                draw(draw)
            }

            for (buffer in vertexBuffers.values) {
                buffer.rotate()
            }
        } finally {
            batchedDraws.clear()
            preparedDraws.clear()
            draws.clear()
        }
        /*?}*/
    }

    /*? if 26.2 {*/
    /*private fun dispatchDraws() {
        applyViewOffsetZLayering()

        try {
            val mainRenderTarget = client.gameRenderer.mainRenderTarget()

            val colorTexture = mainRenderTarget.getColorTextureView()
                ?: error("Main render target color texture view is null")

            val depthTexture = mainRenderTarget.getDepthTextureView()
                ?: error("Main render target depth texture view is null")

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

        renderPass.setPipeline(draw.pipeline)

        renderPass.setUniform(
            "DynamicTransforms",
            setupDynamicTransforms(draw.alphaMultiplier)
        )

        draw.uniform?.let { uniform ->
            renderPass.setUniform(uniform.name, uniform.buffer)
        }

        draw.textureSetup.texure0()?.let { texture ->
            renderPass.bindTexture("Sampler0", texture, draw.textureSetup.sampler0())
        }

        draw.textureSetup.texure1()?.let { texture ->
            renderPass.bindTexture("Sampler1", texture, draw.textureSetup.sampler1())
        }

        draw.textureSetup.texure2()?.let { texture ->
            renderPass.bindTexture("Sampler2", texture, draw.textureSetup.sampler2())
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
    *//*?} else {*/
    
    private fun endBatches() {
        for (draw in batchedDraws.values) {
            val meshData = draw.bufferBuilder.build() ?: continue

            preparedDraws.add(
                PreparedDraw(
                    meshData,
                    draw.pipeline,
                    draw.textureSetup,
                    draw.alphaMultiplier
                )
            )
        }
    }

    private fun setupDraws() {
        setupVertexBuffers()

        val vertexBufferPositions = HashMap<VertexFormat, Int>()

        for (prepared in preparedDraws) {
            val builtBuffer = prepared.builtBuffer
            val drawState = builtBuffer.drawState()
            val format = drawState.format()

            val vertices = vertexBuffers[format]
                ?: error("Missing vertex buffer for format: $format")

            val vertexData = builtBuffer.vertexBuffer()
            val vertexBufferPosition = vertexBufferPositions.getOrDefault(format, 0)
            val remainingVertexBytes = vertexData.remaining()

            copyDataInto(vertices, vertexData, vertexBufferPosition, remainingVertexBytes)

            vertexBufferPositions[format] = vertexBufferPosition + remainingVertexBytes

            draws.add(
                Draw(
                    builtBuffer = builtBuffer,
                    vertices = vertices.currentBuffer(),
                    baseVertex = vertexBufferPosition / format.vertexSize,
                    indexCount = drawState.indexCount(),
                    pipeline = prepared.pipeline,
                    textureSetup = prepared.textureSetup,
                    alphaMultiplier = prepared.alphaMultiplier
                )
            )
        }
    }

    private fun copyDataInto(target: MappableRingBuffer, source: ByteBuffer, position: Int, remainingBytes: Int) {
        val commandEncoder = RenderSystem.getDevice().createCommandEncoder()

        commandEncoder.mapBuffer(
            target.currentBuffer().slice(position.toLong(), remainingBytes.toLong()),
            false,
            true
        ).use { mappedView -> MemoryUtil.memCopy(source, mappedView.data()) }
    }

    private fun setupVertexBuffers() {
        val sizes = collectVertexBufferSizes()

        for ((format, neededSize) in sizes) {
            val current = vertexBuffers[format]

            vertexBuffers[format] = initOrResizeBuffer(
                current,
                "SkyblockCollectionTracker vertex buffer for: $format",
                neededSize
            )
        }
    }

    private fun collectVertexBufferSizes(): Map<VertexFormat, Int> {
        val sizes = HashMap<VertexFormat, Int>()

        for (prepared in preparedDraws) {
            val drawState = prepared.builtBuffer.drawState()
            val format = drawState.format()

            val neededBytes = drawState.vertexCount() * format.vertexSize
            sizes[format] = sizes.getOrDefault(format, 0) + neededBytes
        }

        return sizes
    }

    private fun initOrResizeBuffer(buffer: MappableRingBuffer?, name: String, neededSize: Int): MappableRingBuffer {
        if (buffer == null || buffer.size() < neededSize) {
            buffer?.close()

            return MappableRingBuffer(
                { name },
                GpuBuffer.USAGE_MAP_WRITE or GpuBuffer.USAGE_VERTEX,
                neededSize
            )
        }

        return buffer
    }

    private fun draw(draw: Draw) {
        val indices: GpuBuffer
        val indexType: VertexFormat.IndexType

        if (draw.pipeline.vertexFormatMode == VertexFormat.Mode.QUADS) {
            draw.builtBuffer.sortQuads(
                generalAllocator,
                RenderSystem.getProjectionType().vertexSorting()
            )

            val indexBuffer = draw.builtBuffer.indexBuffer()
                ?: error("Expected an index buffer after sorting quads, but MeshData.indexBuffer() was null")

            indices = draw.pipeline.vertexFormat.uploadImmediateIndexBuffer(indexBuffer)
            indexType = draw.builtBuffer.drawState().indexType()
        } else {
            val shapeIndexBuffer = RenderSystem.getSequentialBuffer(draw.pipeline.vertexFormatMode)

            indices = shapeIndexBuffer.getBuffer(draw.indexCount)
            indexType = shapeIndexBuffer.type()
        }

        draw(draw, indices, indexType)
    }

    private fun draw(draw: Draw, indices: GpuBuffer, indexType: VertexFormat.IndexType) {
        applyViewOffsetZLayering()

        val dynamicTransforms = setupDynamicTransforms(draw.alphaMultiplier)

        RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(
                { "SkyblockCollectionTracker world rendering" },
                getMainColorTexture(),
                OptionalInt.empty(),
                getMainDepthTexture(),
                OptionalDouble.empty()
            ).use { renderPass ->
                renderPass.setPipeline(draw.pipeline)

                RenderSystem.bindDefaultUniforms(renderPass)
                renderPass.setUniform("DynamicTransforms", dynamicTransforms)

                draw.textureSetup.texure0()?.let { texture ->
                    renderPass.bindTexture(
                        "Sampler0",
                        texture,
                        draw.textureSetup.sampler0()
                    )
                }

                draw.textureSetup.texure2()?.let { texture ->
                    renderPass.bindTexture(
                        "Sampler2",
                        texture,
                        draw.textureSetup.sampler2()
                    )
                }

                renderPass.setVertexBuffer(0, draw.vertices)
                renderPass.setIndexBuffer(indices, indexType)

                renderPass.drawIndexed(
                    draw.baseVertex,
                    0,
                    draw.indexCount,
                    1
                )
            }

        draw.builtBuffer.close()
        unapplyViewOffsetZLayering()
    }

    private fun getMainColorTexture(): GpuTextureView {
        return client.mainRenderTarget.colorTextureView ?: error("Main color texture view is null")
    }

    private fun getMainDepthTexture(): GpuTextureView {
        return client.mainRenderTarget.depthTextureView ?: error("Main depth texture view is null")
    }
    /*?}*/

    private fun setupDynamicTransforms(alphaMultiplier: Float): GpuBufferSlice {
        return RenderSystem.getDynamicUniforms()
            .writeTransform(
                /*? if 26.2 {*/
                /*RenderSystem.getModelViewMatrixCopy(),
                Vector4f(1f, 1f, 1f, alphaMultiplier)
                *//*?} else {*/
                
                RenderSystem.getModelViewMatrix(),
                Vector4f(1f, 1f, 1f, alphaMultiplier),
                modelOffset,
                textureMatrix
                /*?}*/
            )
    }

    private fun applyViewOffsetZLayering() {
        val modelViewStack/*? if 26.2 {*//*: Matrix4fStack*//*?}*/ = RenderSystem.getModelViewStack()

        modelViewStack.pushMatrix()
        RenderSystem.getProjectionType().applyLayeringTransform(modelViewStack, 1f)
    }

    private fun unapplyViewOffsetZLayering() {
        RenderSystem.getModelViewStack().popMatrix()
    }

    @JvmStatic
    fun close() {
        /*? if 26.2 {*/
        /*vertexBuffer.close()
        *//*?} else {*/
        
        generalAllocator.close()

        for (allocator in allocators.values) {
            allocator.close()
        }

        for (buffer in vertexBuffers.values) {
            buffer.close()
        }

        allocators.clear()
        vertexBuffers.clear()
        /*?}*/
    }

    /*? if !26.2 {*/
    
    private data class BatchKey(
        val pipeline: RenderPipeline,
        val textureSetup: TextureSetup,
        val alphaMultiplier: Float
    )

    private data class BatchedDraw(
        val bufferBuilder: BufferBuilder,
        val pipeline: RenderPipeline,
        val textureSetup: TextureSetup,
        val alphaMultiplier: Float
    )

    private data class PreparedDraw(
        val builtBuffer: MeshData,
        val pipeline: RenderPipeline,
        val textureSetup: TextureSetup,
        val alphaMultiplier: Float
    )
    /*?}*/

    private data class Draw(
        /*? if 26.2 {*/
        /*val draw: StagedVertexBuffer.Draw,
        val pipeline: RenderPipeline,
        val textureSetup: TextureSetup,
        val alphaMultiplier: Float,
        val instanceCount: Int,
        val uniform: UniformBinding?
        *//*?} else {*/
        
        val builtBuffer: MeshData,
        val vertices: GpuBuffer,
        val baseVertex: Int,
        val indexCount: Int,
        val pipeline: RenderPipeline,
        val textureSetup: TextureSetup,
        val alphaMultiplier: Float
        /*?}*/
    )

    /*? if 26.2 {*/
    /*data class UniformBinding(
        val name: String,
        val buffer: GpuBuffer
    )
    *//*?}*/
}