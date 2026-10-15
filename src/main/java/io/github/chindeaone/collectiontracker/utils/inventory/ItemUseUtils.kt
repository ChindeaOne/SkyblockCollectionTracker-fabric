package io.github.chindeaone.collectiontracker.utils.inventory

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.utils.AbilityUtils
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.parser.AbilityItemParser
import io.github.chindeaone.collectiontracker.utils.parser.ConsumableParser
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item

object ItemUseUtils {

    private var pendingConsumable: HandItemState? = null

    fun onClientTick() {
        if (ModLoader.clientTicks % 4L != 0L) return

        checkIfConsumableConsumed()
    }

    @Suppress("SameReturnValue")
    fun checkHandItemInteraction(player: Player, hand: InteractionHand): InteractionResult {
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

        ConsumableParser.getConsumable(stack.hoverName.string)?.let {
            pendingConsumable = HandItemState(
                buff = it,
                count = stack.count,
                slot = player.inventory.selected,
                timestamp = System.currentTimeMillis()
            )
        }

        return InteractionResult.PASS
    }

    private fun checkIfConsumableConsumed() =
        pendingConsumable?.let { pending ->
            val now = System.currentTimeMillis()

            if (now - pending.timestamp > 2000L) {
                pendingConsumable = null
                return@let
            }

            val player = MinecraftUtils.player ?: return@let
            val currentStack = player.inventory.getItem(pending.slot)

            if (currentStack.isEmpty || currentStack.count < pending.count) {
                ConsumableParser.resetConsumable(pending.buff)
                pendingConsumable = null
            }
        }

    private data class HandItemState(
        val buff: ConsumableParser.Consumables,
        val count: Int,
        val slot: Int,
        val timestamp: Long
    )
}