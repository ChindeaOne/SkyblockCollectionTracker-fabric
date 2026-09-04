/*
    Calculations based on Skyhanni's logic
 */
package io.github.chindeaone.collectiontracker.utils

import java.util.EnumMap
import kotlin.math.floor
import kotlin.math.min

object SkillUtils {
    val SKILL_VALUES = EnumMap<Skills, Double>(Skills::class.java)
    val SKILL_LEVELS = EnumMap<Skills, Int>(Skills::class.java)
    val MAX_SKILL_XP = EnumMap<Skills, Long>(Skills::class.java)

    private const val XP_LVL_60 = 111_672_425.0

    // Precompute max XP for each skill
    fun initializeMaxSkillXp() {
        for (skill in Skills.entries) {
            val maxLevel = getMaxLevelForSkill(skill)
            val maxXp = getTotalXpForMaxLevel(skill, maxLevel)
            MAX_SKILL_XP[skill] = maxXp
        }
    }

    fun getDisplayNames(): List<String> = Skills.entries.map { it.displayName }

    fun isValidSkill(skillName: String): Boolean = Skills.entries.any { it.displayName.equals(skillName, ignoreCase = true) }

    fun updateFromApi(apiValues: Map<String, Double>) {
        if (apiValues.isEmpty()) return
        synchronized(SKILL_VALUES) {
            synchronized(SKILL_LEVELS) {
                for (entry in apiValues.entries) {
                    val skill = fromDisplayName(entry.key)
                    val value = entry.value
                    if (skill != null) {
                        val level = getLevelFromXp(skill, value)
                        SKILL_VALUES[skill] = value
                        SKILL_LEVELS[skill] = level
                    }
                }
            }
        }
        initializeMaxSkillXp()
    }

    fun getSkillValue(skillName: String): Double? = getSkillValue(fromDisplayName(skillName) ?: return null)

    fun getSkillLevel(skillName: String): Int? = getSkillLevel(fromDisplayName(skillName) ?: return null)

    val tamingValue: Double get() = getSkillValue(Skills.TAMING)

    val tamingLevel: Int get() = getSkillLevel(Skills.TAMING)

    fun getMaxXpForSkill(skillName: String): Long {
        val skill = fromDisplayName(skillName)
        return skill?.let { MAX_SKILL_XP[it] ?: 0L } ?: 0L
    }

    private fun getTotalXpForMaxLevel(skill: Skills?, level: Int): Long {
        if (level <= 0) return 0L
        if (skill == Skills.RUNECRAFTING) {
            return getCumulativeFromTable(RUNECRAFTING_LEVELING_XP, level)
        }
        if (level <= 60) {
            return getCumulativeFromTable(LEVELING_XP, level)
        }
        return 0L
    }

    private fun getCumulativeFromTable(table: IntArray, level: Int): Long {
        var total = 0L
        val capped = min(level, table.size)
        for (i in 0..<capped) {
            total += table[i].toLong()
        }
        return total
    }

    fun isSkillMaxed(skillName: String): Boolean = isSkillMaxed(toSkill(skillName) ?: return false)

    private fun isSkillMaxed(skill: Skills): Boolean {
        synchronized(SKILL_LEVELS) {
            val level = SKILL_LEVELS[skill]
            return level != null && level >= getMaxLevelForSkill(skill)
        }
    }

    private fun getSkillValue(skill: Skills): Double = synchronized(SKILL_VALUES) {
        SKILL_VALUES[skill] ?: 0.0
    }

    private fun getSkillLevel(skill: Skills): Int = synchronized(SKILL_LEVELS) {
        SKILL_LEVELS[skill] ?: 0
    }

    private fun toSkill(skillName: String): Skills? = fromDisplayName(skillName)

    private fun fromDisplayName(skillName: String): Skills? = Skills.entries.firstOrNull { it.displayName.equals(skillName, ignoreCase = true) }

    private fun getMaxLevelForSkill(skill: Skills): Int = MaxSkillLevels.valueOf(skill.name).maxLevel

    private fun getLevelFromXp(skill: Skills?, totalXp: Double): Int {
        if (skill == Skills.RUNECRAFTING) {
            return getLevelFromTable(RUNECRAFTING_LEVELING_XP, totalXp)
        }
        if (totalXp < XP_LVL_60) {
            return getLevelFromTable(LEVELING_XP, totalXp)
        }
        return getLevelAbove60(totalXp)
    }

    private fun getLevelFromTable(table: IntArray, totalXp: Double): Int {
        val xp = floor(totalXp).toLong()
        var cumulative = 0L
        for (i in table.indices) {
            cumulative += table[i].toLong()
            if (xp < cumulative) {
                return i
            }
        }
        return table.size
    }

    private fun getLevelAbove60(totalXp: Double): Int {
        var remaining = floor(totalXp - XP_LVL_60).toLong()
        var level = 60
        while (true) {
            val xpNeeded = xpForLevelAbove60(level + 1)
            if (remaining < xpNeeded) break
            remaining -= xpNeeded
            level++
        }
        return level
    }

    private fun xpForLevelAbove60(level: Int): Long {
        if (level <= 60) return 0L
        val k = level - 60 // 1 for lvl 61
        val block = (k - 1) / 10 // which 10-level block
        val posInBlock = (k - 1) % 10 // index inside the block (0-9)
        val baseXp = 7600000L
        val baseSlope = 600000L
        val slope = baseSlope shl block // slope * 2^block
        val blockStartXp = baseXp + (10L * baseSlope * ((1L shl block) - 1))
        return blockStartXp + slope * posInBlock
    }

    private val LEVELING_XP = intArrayOf(
        50, 125, 200, 300, 500, 750, 1000, 1500, 2000, 3500,
        5000, 7500, 10000, 15000, 20000, 30000, 50000, 75000, 100000, 200000,
        300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000, 1100000, 1200000,
        1300000, 1400000, 1500000, 1600000, 1700000, 1800000, 1900000, 2000000, 2100000, 2200000,
        2300000, 2400000, 2500000, 2600000, 2750000, 2900000, 3100000, 3400000, 3700000, 4000000,
        4300000, 4600000, 4900000, 5200000, 5500000, 5800000, 6100000, 6400000, 6700000, 7000000
    )

    private val RUNECRAFTING_LEVELING_XP = intArrayOf(
        50, 100, 125, 160, 200, 250, 315, 400, 500, 625,
        785, 1000, 1250, 1600, 2000, 2465, 3125, 4000, 5000, 6200,
        7800, 9800, 12200, 15300, 19050
    )

    enum class Skills(val displayName: String) {
        MINING("Mining"),
        FORAGING("Foraging"),
        FISHING("Fishing"),
        FARMING("Farming"),
        COMBAT("Combat"),
        ALCHEMY("Alchemy"),
        ENCHANTING("Enchanting"),
        TAMING("Taming"),
        HUNTING("Hunting"),
        CARPENTRY("Carpentry"),
        RUNECRAFTING("Runecrafting"),
        SOCIAL("Social");

        override fun toString(): String {
            return displayName
        }
    }

    enum class MaxSkillLevels(val maxLevel: Int) {
        MINING(60),
        FORAGING(57),
        FISHING(50),
        FARMING(60),
        COMBAT(60),
        ALCHEMY(50),
        ENCHANTING(60),
        TAMING(60),
        HUNTING(50),
        CARPENTRY(50),
        RUNECRAFTING(25),
        SOCIAL(25)
    }
}