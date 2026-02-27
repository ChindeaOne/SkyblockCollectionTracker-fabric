package io.github.chindeaone.collectiontracker.utils.inventory

import io.github.chindeaone.collectiontracker.collections.GemstonesManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
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

object InventoryListener {

    @Volatile
    @JvmStatic
    var isCompacting: Boolean = false
    var lastItemSlot = -1
    var totalItems = 0
    @JvmStatic
    var count = 0
    private var tickCount = 0

    fun onTick(client: Minecraft) {
        if (!HypixelUtils.isOnSkyblock) return
        if (!TrackingHandler.isTracking || !ConfigAccess.isSacksTrackingEnabled() || isCompacting) return

        tickCount++
        if (tickCount % 2 != 0) return

        val player = client.player ?: return

        val inventory = player.inventory
        val collection = CollectionTracker.collection
        val isGemstone: Boolean = GemstonesManager.checkIfGemstone(collection)

        var totalNow = 0
        var lastSlot = -1

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
                lastSlot = i
                totalNow += stack.count
            }
        }
        if (totalItems != totalNow || lastItemSlot != lastSlot) {
            lastItemSlot = lastSlot
            totalItems = totalNow
            count++
        }

        if (count >= 2) isCompacting = true
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