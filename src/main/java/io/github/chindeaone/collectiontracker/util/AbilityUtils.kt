package io.github.chindeaone.collectiontracker.util

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.ConfigManager
import io.github.chindeaone.collectiontracker.util.tab.MiningStatsWidget
import io.github.chindeaone.collectiontracker.util.world.MiningMapping.miningIslands

object AbilityUtils {

    sealed interface AbilitySnapshot {
        val timestamp: Long
        val itemName: String
        val hasAbility: Boolean
    }

    data class PickaxeAbilitySnapshot(
        override val timestamp: Long,
        override val itemName: String,
        val isDrill: Boolean,
        override val hasAbility: Boolean,
        val fuelTank: FuelTank?,
        val hasBlueCheesePart: Boolean
    ) : AbilitySnapshot

    data class AxeAbilitySnapshot(
        override val timestamp: Long,
        override val itemName: String,
        override val hasAbility: Boolean
    ) : AbilitySnapshot

    enum class ForagingAxes(val displayName: String) {
        TREECAPITATOR("Treecapitator"),
        SPRUCE_AXE("Spruce Axe"),
        SERIOUSLY_DAMAGED_AXE("Seriously Damaged Axe"),
        DECENT_AXE("Decent Axe"),
        FIG_HEW("Fig Hew"),
        FIGSTONE_SPLITTER("Figstone Splitter"),
    }

    enum class FuelTank(val cooldownReduction: Double) {
        MITHRIL(0.02),
        TITANIUM(0.04),
        GEMSTONE(0.06),
        PERFECTLY_CUT(0.10)
    }

    enum class PetRarity(val balReductionPerLevel: Double, val crowCommonReductionPerLevel: Double, val crowEpicReductionPerLevel: Double) {
        COMMON(0.0, 0.0007, 0.0),
        UNCOMMON(0.0, 0.0007, 0.0),
        RARE(0.0, 0.0007, 0.0),
        EPIC(0.0, 0.0, 0.0012),
        LEGENDARY(0.001, 0.0, 0.0012),
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

    fun getBaseCooldown(ability: String, cotmLevel: Int, hasBlueCheese: Boolean): Int {
        val effectiveCotm = if (hasBlueCheese && cotmLevel >= 1) cotmLevel + 1 else cotmLevel
        return when (ability) {
            "Mining Speed Boost" -> 120
            "Pickobulus" -> when {
                effectiveCotm >= 2 -> 40
                effectiveCotm >= 1 -> 50
                else -> 60
            }
            "Tunnel Vision" -> when {
                effectiveCotm >= 2 -> 100
                effectiveCotm >= 1 -> 110
                else -> 120
            }
            "Maniac Miner" -> 120
            "Gemstone Infusion" -> 120
            "Sheer Force" -> 120
            else -> 120
        }
    }

    fun getBaseDuration(ability: String, cotmLevel: Int, hasBlueCheese: Boolean): Int {
        val effectiveCotm = if (hasBlueCheese && cotmLevel >= 1) cotmLevel + 1 else cotmLevel
        return when (ability) {
            "Mining Speed Boost" -> when {
                effectiveCotm >= 2 -> 20
                effectiveCotm >= 1 -> 15
                else -> 10
            }
            "Pickobulus" -> 0
            "Tunnel Vision" -> 0
            "Maniac Miner" -> when {
                effectiveCotm >= 2 -> 35
                effectiveCotm >= 1 -> 30
                else -> 25
            }
            "Gemstone Infusion" -> when {
                effectiveCotm >= 2 -> 30
                effectiveCotm >= 1 -> 25
                else -> 20
            }
            "Sheer Force" -> when {
                effectiveCotm >= 2 -> 30
                effectiveCotm >= 1 -> 25
                else -> 20
            }
            else -> 0
        }
    }

    fun getBaseAxeCooldown(ability: String, cotfLevel: Int): Int {
        return when (ability) {
            "Damage Boost" -> when {
                cotfLevel >= 1 -> 110
                else -> 120
            }
            "Axe Toss" -> when {
                cotfLevel >= 1 -> 112
                else -> 120
            }
            "Maniac Slicer" -> when {
                cotfLevel >= 1 -> 58
                else -> 60
            }
            else -> 0
        }
    }

    fun getBaseAxeDuration(ability: String, cotfLevel: Int): Int {
        return when (ability) {
            "Damage Boost" -> 10
            "Axe Toss" -> 10
            "Maniac Slicer" -> when {
                cotfLevel >= 1 -> 15
                else -> 20
            }
            else -> 0
        }
    }

    fun calculateReduction(baseCooldown: Int, snap: PickaxeAbilitySnapshot?, skyMallActive: Boolean): Double {
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
                    if (pet.rarity >= PetRarity.EPIC) {
                        0.03 + (pet.rarity.crowEpicReductionPerLevel * pet.level)
                    } else {
                        0.03 + (pet.rarity.crowCommonReductionPerLevel * pet.level)
                    }
                }
                else -> 0.0
            }
            cooldown *= (1.0 - petReduction)
        }

        // Sky Mall
        if (skyMallActive && miningIslands.contains(MiningStatsWidget.currentMiningIsland)) {
            cooldown *= 0.8
        }
        // TODO: Add Mineshaft Mayhem check next
//        if (mineshaftActive) {
//            cooldown *= 0.75
//        }

        return cooldown.coerceAtLeast(0.0)
    }
}