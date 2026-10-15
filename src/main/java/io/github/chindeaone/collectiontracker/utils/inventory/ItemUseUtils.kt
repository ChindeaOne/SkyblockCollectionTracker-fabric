package io.github.chindeaone.collectiontracker.utils.inventory

import io.github.chindeaone.collectiontracker.utils.AbilityUtils
import io.github.chindeaone.collectiontracker.utils.parser.AbilityItemParser
import io.github.chindeaone.collectiontracker.utils.parser.ConsumableParser
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player

object ItemUseUtils {

    @Suppress("SameReturnValue")
    fun checkHandItemInteraction(player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        if (stack.isEmpty) return InteractionResult.PASS

        // Extract tooltips once
        val tooltipLines = ItemUtils.getTooltipLines(stack, player, normalize = true)
        
        AbilityItemParser.parse(tooltipLines)?.let { snap ->
            AbilityUtils.update(snap)
            return InteractionResult.PASS
        }

        ConsumableParser.getConsumable(stack.hoverName.string)?.let {
            ConsumableParser.resetConsumable(it)
        }

        return InteractionResult.PASS
    }
}