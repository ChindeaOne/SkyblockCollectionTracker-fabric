package io.github.chindeaone.collectiontracker.utils.render

import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.buffers.Std140SizeCalculator
import io.github.chindeaone.collectiontracker.utils.blue
import io.github.chindeaone.collectiontracker.utils.green
import io.github.chindeaone.collectiontracker.utils.red
import net.minecraft.client.renderer.DynamicUniformStorage
import java.nio.ByteBuffer

class ChromaUniform : AutoCloseable {
    private val uniformSize = Std140SizeCalculator().putFloat().putInt().putVec4().putVec4().get()

    private val storage = DynamicUniformStorage<ChromaUniformData>(
        "SCT Chroma UBO",
        uniformSize,
        2
    )

    fun writeUniform(
        timeOffset: Float,
        mode: Int,
        startColor: Int,
        endColor: Int
    ): GpuBufferSlice {
        return storage.writeUniform(
            ChromaUniformData(
                timeOffset,
                mode,
                startColor,
                endColor
            )
        )
    }

    fun clear() {
        storage.endFrame()
    }

    override fun close() {
        storage.close()
    }

    class ChromaUniformData(
        val timeOffset: Float,
        val mode: Int, // 0 = chroma, 1 = prefix gradient
        val startColor: Int,
        val endColor: Int
    ) : DynamicUniformStorage.DynamicUniform {
        override fun write(buffer: ByteBuffer) {
            Std140Builder.intoBuffer(buffer)
                .putFloat(timeOffset)
                .putInt(mode)
                .putVec4(startColor.red() / 255f, startColor.green() / 255f, startColor.blue() / 255f, 1f)
                .putVec4(endColor.red() / 255f, endColor.green() / 255f, endColor.blue() / 255f, 1f)
        }
    }
}