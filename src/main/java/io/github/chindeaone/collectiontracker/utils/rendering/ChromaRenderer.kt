package io.github.chindeaone.collectiontracker.utils.rendering

import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.systems.RenderPass
import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import net.minecraft.client.Minecraft

object ChromaRenderer {

    @JvmStatic
    val chromaUniform = ChromaUniform()
    var chromaBufferSlice: GpuBufferSlice? = null

    @JvmStatic
    fun prepareUniform() {
        val ticks = ModLoader.clientTicks + Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(true)

        val rotation = ConfigAccess.getCustomCWColor().timeForFullRotationInMillis
            .takeIf { it > 0 }
            ?: ConfigAccess.getCustomFWColor().timeForFullRotationInMillis

        val timeOffset = if (rotation > 0) {
            val seconds = ticks / 20f
            (seconds * 1000f / rotation * 3f) % 1f
        } else 0f

        chromaBufferSlice = chromaUniform.writeUniform(timeOffset)
    }

    @JvmStatic
    fun bindUniform(renderPass: RenderPass) {
        chromaBufferSlice?.let {
            renderPass.setUniform(
                "SctChromaUniforms",
                it
            )
        }
    }

    @JvmStatic
    fun clearChromaUniforms() {
        chromaUniform.clear()
        chromaBufferSlice = null
    }
}