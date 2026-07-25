package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.utils.AbilityUtils
import net.minecraft.client.Minecraft
import net.minecraft.world.item.TooltipFlag

object AbilityItemParser {

    private val AXE_REGEX = Regex("\\baxe\\b")
    private val DRILL_REGEX = Regex("\\bdrill\\b")
    private val PICKAXE_REGEX = Regex("\\bpickaxe\\b")
    private val GAUNTLET_REGEX = Regex("\\bgauntlet\\b")

    fun tooltipFlag(): TooltipFlag.Default =
        if (Minecraft.getInstance().options.advancedItemTooltips) TooltipFlag.Default.ADVANCED
        else TooltipFlag.Default.NORMAL

    fun parse(lines: List<String>): AbilityUtils.AbilitySnapshot? {
        var isAxe = false
        var hasBreakingPower = false
        var isDrill = false
        var isPickaxe = false
        var isGauntlet = false

        for (line in lines) {
            if (!isAxe && AXE_REGEX.containsMatchIn(line)) isAxe = true
            if (!hasBreakingPower && line.startsWith("breaking power")) hasBreakingPower = true
            if (!isDrill && DRILL_REGEX.containsMatchIn(line)) isDrill = true
            if (!isPickaxe && PICKAXE_REGEX.containsMatchIn(line)) isPickaxe = true
            if (!isGauntlet && GAUNTLET_REGEX.containsMatchIn(line)) isGauntlet = true
        }

        if (!isAxe && !hasBreakingPower) return null

        val isMiningTool = isDrill || isPickaxe || isGauntlet

        if (isAxe) {
            return AbilityUtils.AxeAbilitySnapshot(
                timestamp = System.currentTimeMillis(),
                hasAbility = true
            )
        }

        if (isMiningTool) {
            var fuelTank: AbilityUtils.FuelTank? = null
            var hasBlueCheese = false

            for (line in lines) {
                if (fuelTank == null) {
                    when {
                        line.contains("perfectly-cut fuel tank") -> fuelTank = AbilityUtils.FuelTank.PERFECTLY_CUT
                        line.contains("gemstone fuel tank") -> fuelTank = AbilityUtils.FuelTank.GEMSTONE
                        line.contains("titanium-infused fuel tank") -> fuelTank = AbilityUtils.FuelTank.TITANIUM
                        line.contains("mithril-infused fuel tank") -> fuelTank = AbilityUtils.FuelTank.MITHRIL
                    }
                }

                if (!hasBlueCheese && line.contains("blue cheese goblin omelette part")) {
                    hasBlueCheese = true
                }

                if (fuelTank != null && hasBlueCheese) break
            }

            return AbilityUtils.PickaxeAbilitySnapshot(
                timestamp = System.currentTimeMillis(),
                isDrill = isDrill,
                hasAbility = true,
                fuelTank = fuelTank,
                hasBlueCheesePart = isDrill && hasBlueCheese
            )
        }
        return null
    }
}