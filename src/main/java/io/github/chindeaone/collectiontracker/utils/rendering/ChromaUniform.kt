package io.github.chindeaone.collectiontracker.utils.rendering

import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import net.minecraft.client.renderer.DynamicUniformStorage
import java.nio.ByteBuffer

class ChromaUniform : AutoCloseable {
    private val uniformSize = Std140SizeCalculator().putFloat().get()

    private val storage =
        DynamicUniformStorage<ChromaUniformData>(
            "SCT Chroma UBO",
            uniformSize,
            2
        )

    fun writeUniform(timeOffset: Float): GpuBufferSlice {
        return storage.writeUniform(
            ChromaUniformData(timeOffset)
        )
    }

    fun clear() {
        storage.endFrame()
    }

    override fun close() {
        storage.close()
    }

    class ChromaUniformData(
        val timeOffset: Float
    ) : DynamicUniformStorage.DynamicUniform {
        override fun write(buffer: ByteBuffer) {
            Std140Builder.intoBuffer(buffer).putFloat(timeOffset)
        }
    }
}