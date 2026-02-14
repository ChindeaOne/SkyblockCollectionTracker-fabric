package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.AbilityUtils
import net.minecraft.client.Minecraft
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

object AbilityItemParser {

    private fun normalize (s: String) = s
        .removeColor()
        .replace("\\p{C}+".toRegex(), " ")
        .replace("\\s+".toRegex(), " ")
        .trim()
        .lowercase()

    private fun tooltipFlag(): TooltipFlag.Default =
        if (Minecraft.getInstance().options.advancedItemTooltips) TooltipFlag.Default.ADVANCED
        else TooltipFlag.Default.NORMAL

    fun snapshot(stack: ItemStack): AbilityUtils.AbilitySnapshot? {
        if (stack.isEmpty) return null
        val player = Minecraft.getInstance().player ?: return null

        // Extract tooltips
        val context = Item.TooltipContext.of(player.level().registryAccess())
        val lines = stack.getTooltipLines(
            context,
            player,
            tooltipFlag()
        ).map { it.string }.map { normalize(it) }

        val hasBreakingPower = lines.any { it.contains("breaking power") }
        val toolTypeLine = lines.findLast { it.contains("\\bdrill\\b".toRegex()) || it.contains("\\bpickaxe\\b".toRegex()) || it.contains("\\bgauntlet\\b".toRegex()) }
        val isDrill = toolTypeLine?.contains("drill") == true
        val isPickaxeOrDrill = hasBreakingPower && toolTypeLine != null
        val isAxe = lines.findLast { it.contains("\\baxe\\b".toRegex()) }

        // Logic for Axes
        if (isAxe != null) {
            return AbilityUtils.AxeAbilitySnapshot(
                timestamp = System.currentTimeMillis(),
                hasAbility = true
            )
        }

        // Logic for Pickaxes/Drills
        if (isPickaxeOrDrill) {
            val fuelTank = when {
                lines.any { it.contains("perfectly-cut fuel tank") } -> AbilityUtils.FuelTank.PERFECTLY_CUT
                lines.any { it.contains("gemstone fuel tank") }      -> AbilityUtils.FuelTank.GEMSTONE
                lines.any { it.contains("titanium-infused fuel tank") } -> AbilityUtils.FuelTank.TITANIUM
                lines.any { it.contains("mithril-infused fuel tank") }  -> AbilityUtils.FuelTank.MITHRIL
                else -> null
            }

            val hasBlueCheese = lines.any { it.contains("blue cheese goblin omelette part") }

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