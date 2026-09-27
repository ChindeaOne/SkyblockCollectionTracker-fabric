package io.github.chindeaone.collectiontracker.utils.world.deployable

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.enableDeployable
import io.github.chindeaone.collectiontracker.config.deployableOutOfRangeWarning
import io.github.chindeaone.collectiontracker.config.showDeployableTitle
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils
import io.github.chindeaone.collectiontracker.utils.world.EntityUtils
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

/**
* This Kotlin object is based on SkyHanni's implementation.
*/
object DeployableTracker {

    private val TIME_REGEX = Regex("""(\d+)s""")

    var activeDeployable: TrackedDeployable? = null

    private var isInMineshaft: Boolean = false
    private val trackedDeployables = mutableListOf<TrackedDeployable>()

    fun onClientTick(client: Minecraft) {
        if (!enableDeployable) return
        if (ModLoader.clientTicks % 5L != 0L) return

        val level = client.level ?: return
        val player = client.player ?: return

        isInMineshaft = IslandTracker.currentMiningIsland == "Mineshaft"

        val previous = activeDeployable

        updateTrackedDeployables(level, player)

        if (previous != null && previous.remainingSeconds <= 0) {
            notifyExpiration(previous)
        }

        trackedDeployables.removeIf { it.remainingSeconds <= 0 }

        val current = getBestDeployable(player)

        if (current != null && previous != null && shouldNotifyOutOfRange(previous, current, player)) {
            notifyOutOfRange(previous)
        }

        if (current == null) {
            activeDeployable = null
            return
        }

        activeDeployable = current
    }

    private fun updateTrackedDeployables(level: Level, player: Player) {
        val nearbyEntities = EntityUtils.getArmorStandsAroundPlayer(level, player.position(), 30.0)

        for (entity in nearbyEntities) {
            val type = getDeployableType(entity) ?: continue

            if (trackedDeployables.any { it.entity.id == entity.id }) {
                continue
            }

            val seconds = getRemainingSeconds(entity)

            trackedDeployables += TrackedDeployable(
                entity = entity,
                type = type,
                expiresAt = System.currentTimeMillis() + seconds * 1000L
            )
        }
    }

    private fun getDeployableType(entity: ArmorStand): MiningDeployable? {
        val name = entity.name.string.removeColor()

        return MiningDeployable.entries.firstOrNull {
            name.contains(it.deployableName, ignoreCase = true)
        }
    }

    private fun getRemainingSeconds(entity: ArmorStand): Int {
        val name = entity.name.string.removeColor()
        return TIME_REGEX.find(name)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun getBestDeployable(player: Player): TrackedDeployable? {
        return trackedDeployables
            .filter { it.entity.isAlive }
            .filter { it.remainingSeconds > 0 }
            .filter { isPlayerInRange(it, player) }
            .maxWithOrNull(
                compareBy<TrackedDeployable> { it.type.tier }
                    .thenBy { it.remainingSeconds }
            )
    }

    private fun isPlayerInRange(deployable: TrackedDeployable, player: Player): Boolean {
        return (isInMineshaft && deployable.type.mineshaftGlobalRange) || deployable.entity.distanceToSqr(player) <= 30.0 * 30.0
    }

    private fun notifyExpiration(deployable: TrackedDeployable) {
        if (!showDeployableTitle) return

        RenderUtils.showTitle(Component.literal("${deployable.type.displayName} §cExpired!"))
    }

    private fun shouldNotifyOutOfRange(previous: TrackedDeployable, current: TrackedDeployable, player: Player): Boolean {
        if (previous.remainingSeconds <= 0) return false
        if (isPlayerInRange(previous, player)) return false
        if (current.entity.id == previous.entity.id) return false
        if (current.type.tier >= previous.type.tier) return false

        return !(isInMineshaft && previous.type.mineshaftGlobalRange)
    }

    private fun notifyOutOfRange(deployable: TrackedDeployable) {
        if (!deployableOutOfRangeWarning) return

        RenderUtils.showTitle(Component.literal("${deployable.type.displayName} §cOut of range!"))
    }

    fun reset() {
        trackedDeployables.clear()
        activeDeployable = null
    }
}