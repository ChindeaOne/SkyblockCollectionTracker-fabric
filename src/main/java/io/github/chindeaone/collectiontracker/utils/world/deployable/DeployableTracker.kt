package io.github.chindeaone.collectiontracker.utils.world.deployable

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.enableDeployable
import io.github.chindeaone.collectiontracker.config.deployableOutOfRangeWarning
import io.github.chindeaone.collectiontracker.config.showDeployableTitle
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.fromNow
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils
import io.github.chindeaone.collectiontracker.utils.world.EntityUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.decoration.ArmorStand
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

/**
* This Kotlin object is based on SkyHanni's implementation.
*/
object DeployableTracker {

    var activeDeployable: MiningDeployable? = null

    private val trackedDeployables = mutableListOf<MiningDeployable>()

    fun onClientTick(client: Minecraft) {
        if (!enableDeployable) return
        if (ModLoader.clientTicks % 5L != 0L) return

        val level = client.level ?: return
        val player = client.player ?: return

        EntityUtils.getArmorStandsAroundPlayer(level, player.position(), 30.0).forEach { addDeployables(it) }

        val current = getBestDeployable()
        val previous = activeDeployable

        if (previous != null && current == null) {
            notifyPlayer(previous)
        }

        activeDeployable = current

        trackedDeployables.removeIf { !it.isAlive() }
        MiningDeployable.entries.forEach { if (!it.isAlive()) it.reset() }
    }

    private fun addDeployables(entity: ArmorStand) {
        val entityName = entity.name.string.removeColor()

        for (deployable in MiningDeployable.entries) {
            if (!deployable.isInPlayerRange(entity)) continue

            val match = deployable.pattern.find(entityName) ?: continue
            val deployableTime = match.groupValues[1].toLongOrNull() ?: continue
            val timeLeft = deployableTime.seconds.fromNow()

            val expiryTime = deployable.expiryTime
            if (expiryTime != null) {
                val diff = abs((expiryTime - timeLeft).inWholeSeconds)
                if (diff <= 1) continue

                if (expiryTime > timeLeft && deployable.isActive()) continue
            }

            deployable.entity = entity
            deployable.expiryTime = timeLeft

            if (!trackedDeployables.contains(deployable)) {
                trackedDeployables.add(deployable)
            }
        }
    }

    private fun getBestDeployable(): MiningDeployable? {
        return trackedDeployables
            .filter { it.isActive() }
            .maxWithOrNull(
                compareBy<MiningDeployable> { it.tier }
                    .thenBy { it.expiryTime }
            )
    }

    private fun notifyPlayer(previous: MiningDeployable?) {
        if (previous == null) return

        if (!previous.isAlive()) {
            notifyExpiration(previous)
        } else if (!previous.isInPlayerRange()) {
            notifyOutOfRange(previous)
        }
    }

    private fun notifyExpiration(deployable: MiningDeployable) {
        if (!showDeployableTitle) return

        RenderUtils.showTitle(Component.literal("${deployable.displayName} §cExpired!"))
    }

    private fun notifyOutOfRange(deployable: MiningDeployable) {
        if (!deployableOutOfRangeWarning) return

        RenderUtils.showTitle(Component.literal("${deployable.displayName} §cOut of range!"))
    }

    fun reset() {
        trackedDeployables.clear()
        activeDeployable = null
        MiningDeployable.entries.forEach { it.reset() }
    }
}