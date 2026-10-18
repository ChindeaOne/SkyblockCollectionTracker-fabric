package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.utils.ability.AbilityUtils

object AbilityItemParser {

    private val AXE_REGEX = Regex("\\baxe\\b")
    private val DRILL_REGEX = Regex("\\bdrill\\b")

    fun parse(lines: List<String>): AbilityUtils.AbilitySnapshot? {
        var isAxe = false
        var hasBreakingPower = false
        var isDrill = false

        for (line in lines) {
            if (AXE_REGEX.containsMatchIn(line)) isAxe = true
            if (line.startsWith("breaking power")) hasBreakingPower = true
            if (DRILL_REGEX.containsMatchIn(line)) isDrill = true
        }

        if (!isAxe && !hasBreakingPower) return null

        val abilityType = if (isAxe) AbilityUtils.AbilityType.AXE else AbilityUtils.AbilityType.PICKAXE

        var fuelTank: AbilityUtils.FuelTank? = null
        var hasBlueCheese = false

        if (isDrill) {
            for (line in lines) {
                if (fuelTank == null) {
                    when {
                        line.contains("perfectly-cut fuel tank") -> fuelTank = AbilityUtils.FuelTank.PERFECTLY_CUT
                        line.contains("gemstone fuel tank") -> fuelTank = AbilityUtils.FuelTank.GEMSTONE
                        line.contains("titanium-infused fuel tank") -> fuelTank = AbilityUtils.FuelTank.TITANIUM
                        line.contains("mithril-infused fuel tank") -> fuelTank = AbilityUtils.FuelTank.MITHRIL
                    }
                }

                if (line.contains("blue cheese goblin omelette part")) {
                    hasBlueCheese = true
                }

                if (fuelTank != null && hasBlueCheese) break
            }
        }

        return AbilityUtils.AbilitySnapshot(
            abilityType = abilityType,
            fuelTank = if (abilityType == AbilityUtils.AbilityType.PICKAXE) fuelTank else null,
            hasBlueCheesePart = hasBlueCheese
        )
    }
}