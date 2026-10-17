package io.github.chindeaone.collectiontracker.utils.world.deployable

import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.world.EntityUtils
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import net.minecraft.world.entity.decoration.ArmorStand

/**
 * This Kotlin enum is based on SkyHanni's implementation.
 */
enum class MiningDeployable(
    deployableName: String,
    val displayName: String,
    val tier: Int,
    val mineshaftGlobalRange: Boolean = false,
    var entity: ArmorStand? = null,
    var expiryTime: EpochMark? = null
) {
    DWARVEN_LANTERN("Dwarven Lantern", "§fDwarven Lantern", 1),
    MITHRIL_LANTERN("Mithril Lantern", "§aMithril Lantern", 2),
    TITANIUM_LANTERN("Titanium Lantern", "§9Titanium Lantern", 3),
    GLACITE_LANTERN("Glacite Lantern", "§5Glacite Lantern", 4, true),
    WILL_O_WISP("Will-o'-wisp", "§6Will-o'-wisp", 5, true);

    val pattern = Regex("""$deployableName (?<time>\d+)s""")

    fun isInPlayerRange(entity: ArmorStand): Boolean {
        return hasMineshaftGlobalRange() || EntityUtils.isEntityInPlayerRange(entity)
    }

    fun isInPlayerRange(): Boolean {
        if (hasMineshaftGlobalRange()) return true
        val entity = entity ?: return false
        if (entity.isRemoved) return false
        return EntityUtils.isEntityInPlayerRange(entity)
    }

    fun hasMineshaftGlobalRange(): Boolean {
        return mineshaftGlobalRange && IslandTracker.currentMiningIsland == "Mineshaft"
    }

    fun isActive(): Boolean = isAlive() && isInPlayerRange()

    fun isAlive(): Boolean = expiryTime?.isPassed == false

    fun reset() {
        entity = null
        expiryTime = null
    }
}