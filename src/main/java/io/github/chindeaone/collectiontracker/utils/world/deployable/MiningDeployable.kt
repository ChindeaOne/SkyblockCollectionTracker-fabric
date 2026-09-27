package io.github.chindeaone.collectiontracker.utils.world.deployable

import net.minecraft.world.entity.decoration.ArmorStand

enum class MiningDeployable(val deployableName: String, val displayName: String, val tier: Int, val mineshaftGlobalRange: Boolean = false) {
    DWARVEN_LANTERN("Dwarven Lantern", "§fDwarven Lantern", 1),
    MITHRIL_LANTERN("Mithril Lantern", "§aMithril Lantern", 2),
    TITANIUM_LANTERN("Titanium Lantern", "§9Titanium Lantern", 3),
    GLACITE_LANTERN("Glacite Lantern", "§5Glacite Lantern", 4, true),
    WILL_O_WISP("Will-o'-wisp", "§6Will-o'-wisp", 5, true);
}

data class TrackedDeployable(
    val type: MiningDeployable,
    val entity: ArmorStand,
    val expiresAt: Long
) {
    val remainingSeconds: Int
        get() = ((expiresAt - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)
}




