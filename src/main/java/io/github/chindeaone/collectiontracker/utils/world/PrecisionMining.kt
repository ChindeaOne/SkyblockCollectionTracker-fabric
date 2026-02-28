package io.github.chindeaone.collectiontracker.utils.world

import com.mojang.blaze3d.systems.RenderSystem
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.Minecraft
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.awt.Color

object PrecisionMining {

    private val notLookingParticle = ParticleTypes.CRIT
    private val lookingParticle = ParticleTypes.HAPPY_VILLAGER

    private var isLooking = false
    private var activeParticlePos: Vec3? = null

    fun render(context: WorldRenderContext) {
        if (!RenderSystem.isOnRenderThread()) return
        if (!HypixelUtils.isOnSkyblock) return
        if (!ConfigAccess.isPrecisionMiningHighlightEnabled()) return
        if (BlockWatcher.precisionMiningBlockType.isEmpty()) {
            isLooking = false
            activeParticlePos = null
            return
        }

        val mc = Minecraft.getInstance()
        if (!mc.options.keyAttack.isDown) {
            isLooking = false
            activeParticlePos = null
            return
        }

        val camera = context.worldState().cameraRenderState
        val buffers = context.consumers()

        val pos = activeParticlePos ?: return

        val box = AABB(
            pos.x - 0.07, pos.y - 0.07, pos.z - 0.07,
            pos.x + 0.07, pos.y + 0.07, pos.z + 0.07
        )

        val color = if (isLooking) Color.GREEN else Color.RED
        BlockOutline.renderBox(buffers, box, camera, color)
    }

    @JvmStatic
    fun handleParticles(options: ParticleOptions, x: Double, y: Double, z: Double) {
        if (!HypixelUtils.isOnSkyblock) return
        if (options.type == lookingParticle) {
            activeParticlePos = Vec3(x, y, z)
            isLooking = true
        } else if (options.type == notLookingParticle) {
            activeParticlePos = Vec3(x, y, z)
            isLooking = false
        }
    }
}