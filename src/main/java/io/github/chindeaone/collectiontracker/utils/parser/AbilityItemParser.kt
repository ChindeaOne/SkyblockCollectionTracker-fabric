package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.AbilityUtils
import net.minecraft.client.Minecraft
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

object AbilityItemParser {

    private val DRILL = Regex("\\bDrill\\b", RegexOption.IGNORE_CASE)
    private val PICKAXE = Regex("\\bPickaxe\\b|\\bGauntlet\\b", RegexOption.IGNORE_CASE)

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

        val rawName = stack.hoverName.string
        val name = normalize(rawName)
        val isDrill = DRILL.containsMatchIn(name)
        val isPickaxeOrDrill = isDrill || PICKAXE.containsMatchIn(name)
        val isAxe = AbilityUtils.ForagingAxes.entries.any { name.contains(it.displayName.lowercase()) }

        // Extract tooltips
        val context = Item.TooltipContext.of(player.level().registryAccess())
        val lines = stack.getTooltipLines(
            context,
            player,
            tooltipFlag()
        ).map { it.string }.map { normalize(it) }

        // Logic for Axes
        if (isAxe && !isPickaxeOrDrill) {
            return AbilityUtils.AxeAbilitySnapshot(
                timestamp = System.currentTimeMillis(),
                itemName = rawName,
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
                itemName = rawName,
                isDrill = isDrill,
                hasAbility = true,
                fuelTank = fuelTank,
                hasBlueCheesePart = isDrill && hasBlueCheese
            )
        }

        return null
    }
}