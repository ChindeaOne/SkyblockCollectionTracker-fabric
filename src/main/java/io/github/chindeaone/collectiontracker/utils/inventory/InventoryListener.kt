package io.github.chindeaone.collectiontracker.utils.inventory

import io.github.chindeaone.collectiontracker.utils.AbilityUtils
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.parser.AbilityItemParser
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item

object InventoryListener {

    @Suppress("SameReturnValue")
    fun checkHandItem(player: Player, hand: InteractionHand): InteractionResult {
        if (!HypixelUtils.isOnSkyblock) return InteractionResult.PASS

        val stack = player.getItemInHand(hand)
        if (stack.isEmpty) return InteractionResult.PASS

        // Extract tooltips once
        val context = Item.TooltipContext.of(player.level().registryAccess())
        val lines = stack.getTooltipLines(
            context,
            player,
            AbilityItemParser.tooltipFlag()
        ).map { it.string }.map { StringUtils.normalizeText(it) }

        AbilityItemParser.parse(lines)?.let { snap ->
            AbilityUtils.update(snap)
            return InteractionResult.PASS
        }

        val itemName = lines.firstOrNull()
        TemporaryBuffsParser.resetConsumable(itemName)

        return InteractionResult.PASS
    }
}