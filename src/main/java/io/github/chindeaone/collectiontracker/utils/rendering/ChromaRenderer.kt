package io.github.chindeaone.collectiontracker.utils.rendering

import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.systems.RenderPass
import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import net.minecraft.client.Minecraft
import java.awt.Color

object ChromaRenderer {

    @JvmStatic
    val chromaUniform = ChromaUniform()
    var normalChromaSlice: GpuBufferSlice? = null
    var prefixGradientSlice: GpuBufferSlice? = null

    private fun computeTimeOffset(mode: Int): Float {
        val ticks = ModLoader.clientTicks + Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(true)

        val rotation: Long = if (mode == 0) {
            val cw = ConfigAccess.getCustomCWColor().timeForFullRotationInMillis.toLong()
            if (cw > 0) cw else ConfigAccess.getCustomFWColor().timeForFullRotationInMillis.toLong()
        } else {
            12000L
        }

        return if (rotation > 0L) {
            val seconds = ticks / 20f
            (seconds * 1000f / rotation.toFloat() * 3f) % 1f
        } else {
            0f
        }
    }

    @JvmStatic
    fun prepareUniforms() {
        // Normal chroma
        normalChromaSlice = chromaUniform.writeUniform(
            computeTimeOffset(0),
            0,
            Color.BLACK,
            Color.BLACK
        )

        // Prefix chroma
        prefixGradientSlice = chromaUniform.writeUniform(
            computeTimeOffset(1),
            1,
            ColorUtils.GRADIENT_START_COLOR,
            ColorUtils.GRADIENT_END_COLOR
        )
    }

    @JvmStatic
    fun bindNormalChroma(renderPass: RenderPass) {
        normalChromaSlice?.let {
            renderPass.setUniform("SctChromaUniforms", it)
        }
    }

    @JvmStatic
    fun bindPrefixGradient(renderPass: RenderPass) {
        prefixGradientSlice?.let {
            renderPass.setUniform("SctChromaUniforms", it)
        }
    }

    @JvmStatic
    fun clearChromaUniforms() {
        chromaUniform.clear()
        normalChromaSlice = null
        prefixGradientSlice = null
    }
}