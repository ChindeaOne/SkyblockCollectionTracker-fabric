package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping.miningIslands

object AbilityUtils {

    sealed interface AbilitySnapshot {
        val timestamp: Long
        val hasAbility: Boolean
    }

    data class PickaxeAbilitySnapshot(
        override val timestamp: Long,
        val isDrill: Boolean,
        override val hasAbility: Boolean,
        val fuelTank: FuelTank?,
        val hasBlueCheesePart: Boolean
    ) : AbilitySnapshot

    data class AxeAbilitySnapshot(
        override val timestamp: Long,
        override val hasAbility: Boolean
    ) : AbilitySnapshot

    enum class FuelTank(val cooldownReduction: Double) {
        MITHRIL(0.02),
        TITANIUM(0.04),
        GEMSTONE(0.06),
        PERFECTLY_CUT(0.10)
    }

    @Volatile
    var lastPickaxeSnap: PickaxeAbilitySnapshot? = null

    @Volatile
    var lastAxeSnap: AxeAbilitySnapshot? = null
    private const val MAX_AGE_MS = 3000L // 3 seconds

    var isMayhemCooldown: Boolean = false

    fun update(s: AbilitySnapshot) {
        when (s) {
            is PickaxeAbilitySnapshot -> lastPickaxeSnap = s
            is AxeAbilitySnapshot -> lastAxeSnap = s
        }
    }

    fun recentOrNull(now: Long = System.currentTimeMillis()): PickaxeAbilitySnapshot? =
        lastPickaxeSnap?.takeIf { now - it.timestamp <= MAX_AGE_MS }

    fun recentOrNullAxe(now: Long = System.currentTimeMillis()): AxeAbilitySnapshot? =
        lastAxeSnap?.takeIf { now - it.timestamp <= MAX_AGE_MS }

    fun getBaseCooldown(ability: String, abilityLevel: Int, hasBlueCheese: Boolean): Int {
        val effectiveLevel = if (hasBlueCheese) abilityLevel + 1 else abilityLevel
        return when (ability) {
            "Mining Speed Boost" -> 120
            "Pickobulus" -> when (effectiveLevel) {
                3 -> 40
                2 -> 50
                1 -> 60
                else -> 0
            }
            "Tunnel Vision" -> when (effectiveLevel) {
                3 -> 100
                2 -> 110
                1 -> 120
                else -> 0
            }
            "Maniac Miner" -> 120
            "Gemstone Infusion" -> 120
            "Sheer Force" -> 120
            else -> 120
        }
    }

    fun getBaseDuration(ability: String, abilityLevel: Int, hasBlueCheese: Boolean): Int {
        val effectiveLevel = if (hasBlueCheese) abilityLevel + 1 else abilityLevel
        return when (ability) {
            "Mining Speed Boost" -> when (effectiveLevel) {
                3 -> 20
                2 -> 15
                1 -> 10
                else -> 0
            }
            "Pickobulus" -> 0
            "Tunnel Vision" -> 30
            "Maniac Miner" -> when (effectiveLevel) {
                3 -> 35
                2 -> 30
                1 -> 25
                else -> 0
            }
            "Gemstone Infusion" -> when (effectiveLevel) {
                3 -> 30
                2 -> 25
                1 -> 20
                else -> 0
            }
            "Sheer Force" -> when (effectiveLevel) {
                3 -> 30
                2 -> 25
                1 -> 20
                else -> 0
            }
            else -> 0
        }
    }

    fun getBaseAxeCooldown(ability: String, abilityLevel: Int): Int {
        return when (ability) {
            "Damage Boost" -> when (abilityLevel) {
                2 -> 110
                1 -> 120
                else -> 0
            }
            "Axe Toss" -> when (abilityLevel) {
                2 -> 112
                1 -> 120
                else -> 0
            }
            "Maniac Slicer" -> when (abilityLevel) {
                2 -> 58
                1 -> 60
                else -> 0
            }
            else -> 0
        }
    }

    fun getBaseAxeDuration(ability: String, abilityLevel: Int): Int {
        return when (ability) {
            "Damage Boost" -> 10
            "Axe Toss" -> 10
            "Maniac Slicer" -> when (abilityLevel) {
                2 -> 20
                1 -> 15
                else -> 0
            }
            else -> 0
        }
    }

    fun calculateReduction(baseCooldown: Int, snap: PickaxeAbilitySnapshot?, skyMallActive: Boolean, abilityName: String): Double {
        var cooldown = baseCooldown.toDouble()

        // Fuel Tank
        if (snap?.isDrill == true && snap.fuelTank != null) {
            cooldown *= (1.0 - (snap.fuelTank.cooldownReduction))
        }

        if (ConfigAccess.hasCooldownAttribute()) {
            cooldown *= (1.0 - ConfigAccess.getAttributeLevel() / 100f)
        }

        // Sky Mall
        if (skyMallActive && miningIslands.contains(IslandTracker.currentMiningIsland)) {
            cooldown *= if (abilityName == "Pickobulus") {
                0.765 // apparently it's more for pickobulus
            } else {
                0.8
            }
        }

        // Mayhem cooldown reduction
        if (IslandTracker.currentMiningIsland == "Mineshaft" && isMayhemCooldown) {
            cooldown *= 0.75
        }

        return cooldown.coerceAtLeast(0.0)
    }
}