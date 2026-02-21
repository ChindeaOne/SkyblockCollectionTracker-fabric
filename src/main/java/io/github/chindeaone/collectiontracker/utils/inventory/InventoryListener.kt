package io.github.chindeaone.collectiontracker.utils.inventory

import io.github.chindeaone.collectiontracker.collections.GemstonesManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.utils.AbilityUtils
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.parser.AbilityItemParser
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item

/**
    * This class is used to check for the tracked collection item.
    * If it's found, it means that sacks messages represent compacted items.
 */
object InventoryListener {

    @Volatile
    @JvmStatic
    var hasItem: Boolean = false

    fun onTick(client: Minecraft) {
        if (!HypixelUtils.isOnSkyblock) return
        if (hasItem || !TrackingHandler.isTracking) return

        val player = client.player ?: return

        val inventory = player.inventory
        val collection = CollectionTracker.collection
        val isGemstone: Boolean = GemstonesManager.checkIfGemstone(collection)

        for (i in 0 until 36) {
            val stack = inventory.getItem(i)
            if (stack.isEmpty) continue

            val itemName = StringUtils.normalizeText(stack.itemName.string)

            val match = if (isGemstone) {
                matchesGemstone(itemName, collection)
            } else {
                matchesNormal(itemName, collection)
            }

            if (match) {
                hasItem = true
                println("[SCT]: Found tracked collection item '$collection' in inventory.")
                return
            }
        }
    }

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
        println("Checking hand item: $itemName")
        TemporaryBuffsParser.resetConsumable(itemName)

        return InteractionResult.PASS
    }

    private fun matchesNormal(itemName: String, collection: String): Boolean {
        return itemName.startsWith(collection)
    }

    private fun matchesGemstone(itemName: String, collection: String): Boolean {
        return itemName.endsWith(" $collection gemstone")
    }
}