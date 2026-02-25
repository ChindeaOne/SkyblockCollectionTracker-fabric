package io.github.chindeaone.collectiontracker.utils

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.ConfigManager
import io.github.chindeaone.collectiontracker.utils.tab.MiningStatsWidget
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

    enum class PetRarity(val balReductionPerLevel: Double, val crowReductionPerLevel: Double) {
        COMMON(0.0, 0.0007),
        UNCOMMON(0.0, 0.0007),
        RARE(0.0, 0.0007),
        EPIC(0.0,0.0012),
        LEGENDARY(0.001,0.0012),
        MYTHIC(0.0, 0.0) // for other mythic pets
    }

    data class Pet(
        @Expose val name: String,
        @Expose val level: Int,
        @Expose val rarity: PetRarity,
        @Expose val timestamp: Long = System.currentTimeMillis(),
        @Expose val isManual: Boolean = false
    )

    @Volatile
    var lastPickaxeSnap: PickaxeAbilitySnapshot? = null

    @Volatile
    var lastAxeSnap: AxeAbilitySnapshot? = null
    private const val MAX_AGE_MS = 3000L // 3 seconds

    @Volatile
    var lastPet: Pet? = null
    private val gson = ConfigManager.gson

    var isMayhemCooldown: Boolean = false

    fun update(s: AbilitySnapshot) {
        when (s) {
            is PickaxeAbilitySnapshot -> lastPickaxeSnap = s
            is AxeAbilitySnapshot -> lastAxeSnap = s
        }
    }

    fun updatePet(pet: Pet) {
        lastPet = pet
        ConfigHelper.setLastPet(gson.toJson(pet))
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
            "Tunnel Vision" -> 0
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
                2 -> 15
                1 -> 20
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

        // Pet swap
        lastPet?.let { pet ->
            val petReduction = when {
                pet.name.equals("Bal", ignoreCase = true) && pet.rarity >= PetRarity.LEGENDARY -> {
                    pet.rarity.balReductionPerLevel * pet.level
                }
                pet.name.equals("Crow", ignoreCase = true) -> {
                    0.03 + (pet.rarity.crowReductionPerLevel * pet.level)
                }
                else -> 0.0
            }
            cooldown *= (1.0 - petReduction)
        }
        // Sky Mall
        if (skyMallActive && miningIslands.contains(MiningStatsWidget.currentMiningIsland)) {
            cooldown *= if (abilityName == "Pickobulus") {
                0.765 // apparently it's more for pickobulus
            } else {
                0.8
            }
        }

        // Mayhem cooldown reduction
        if (MiningStatsWidget.currentMiningIsland == "Mineshaft" && isMayhemCooldown) {
            cooldown *= 0.75
        }

        return cooldown.coerceAtLeast(0.0)
    }

    fun calculateAxeReduction(baseCooldown: Int): Double {
        val cooldown = baseCooldown.toDouble()

        lastPet?.let { pet ->
            val petReduction = when {
                pet.name.equals("Crow", ignoreCase = true) -> {
                    0.03 + (pet.rarity.crowReductionPerLevel * pet.level)
                }
                else -> 0.0
            }
            return cooldown * (1.0 - petReduction).coerceAtLeast(0.0)
        }
        return cooldown
    }
}