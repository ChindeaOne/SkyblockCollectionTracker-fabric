package io.github.chindeaone.collectiontracker.utils.inventory

import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

object ItemUtils {

    private val tooltipFlag: TooltipFlag.Default =
        if (MinecraftUtils.options.advancedItemTooltips) TooltipFlag.Default.ADVANCED
        else TooltipFlag.Default.NORMAL

    fun getTooltipLines(stack: ItemStack, player: Player, normalize: Boolean = false): List<String> {
        val context = Item.TooltipContext.of(player.level().registryAccess())

        val lines = stack.getTooltipLines(context, player, tooltipFlag).map { it.string }
        if (normalize) {
            return lines.map { StringUtils.normalizeText(it) }
        }

        return lines
    }
}