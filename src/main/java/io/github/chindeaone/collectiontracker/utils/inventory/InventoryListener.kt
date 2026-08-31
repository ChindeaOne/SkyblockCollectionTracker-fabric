package io.github.chindeaone.collectiontracker.utils.inventory

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates
import io.github.chindeaone.collectiontracker.utils.AbilityUtils
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.parser.AbilityItemParser
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import net.minecraft.client.Minecraft
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item

object InventoryListener {

    private val slotSkipMap = mutableMapOf<Int, Long>()
    private val slotMatchHits = mutableMapOf<Int, Int>()
    private val lastInventoryState = mutableMapOf<Int, Int>()
    private val lastItemNames = mutableMapOf<Int, String>()

    private var pendingConsumable: HandItemState? = null

    fun onClientTick(client: Minecraft) {
        if (!HypixelUtils.isOnSkyblock) return
        if (ModLoader.clientTicks % 4L != 0L) return

        if (/*? if 26.2 {*/ /*client.gui.screen() *//*?} else {*/ client.screen /*?}*/ != null) {
            lastInventoryState.clear()
            slotMatchHits.clear()
            slotSkipMap.clear()
            return
        }

        checkIfConsumableConsumed(client)

        val isTracking = TrackingHandler.isTracking
        val isMultiTracking = MultiTrackingHandler.isMultiTracking

        val isTrackingPaused = TrackingHandler.isPaused
        val isMultiTrackingPaused = MultiTrackingHandler.isMultiPaused

        if (!isTracking && !isMultiTracking) {
            lastInventoryState.clear()
            slotMatchHits.clear()
            slotSkipMap.clear()
            return
        }

        if (isTrackingPaused || isMultiTrackingPaused || !IslandTracker.isInRift) return
        if (ConfigAccess.isApiTrackingEnabled()) return

        val player = client.player ?: return
        val inventory = player.inventory
        val now = System.currentTimeMillis()

        val currentSoloColl = CollectionTracker.collection
        val isSoloRift = isTracking && CollectionsManager.isRiftCollection(currentSoloColl)

        val multiRiftCollections = if (isMultiTracking) {
            CollectionTracker.collectionList.filter { CollectionsManager.isRiftCollection(it) }
        } else emptyList()

        if (!isSoloRift && multiRiftCollections.isEmpty()) return

        val multiGains = mutableMapOf<String, Long>()
        var soloGainToProcess = 0L

        for (i in 0 until 36) {
            val stack = inventory.getItem(i)
            if (stack.isEmpty) {
                lastInventoryState[i] = 0
                slotMatchHits.remove(i)
                slotSkipMap.remove(i)
                lastItemNames.remove(i)
                continue
            }

            val itemName = StringUtils.normalizeText(stack.hoverName.string)

            val previousItemName = lastItemNames[i]
            if (previousItemName != itemName) {
                slotSkipMap.remove(i)
                slotMatchHits.remove(i)
                lastItemNames[i] = itemName
            }

            if ((slotSkipMap[i] ?: 0L) > now) continue

            val currentCount = stack.count

            val isFirstTime = !lastInventoryState.containsKey(i)
            val previousCount = lastInventoryState[i] ?: currentCount
            val gainCount = if (isFirstTime) 0 else (currentCount - previousCount).coerceAtLeast(0)

            var matchedAny = false

            if (isSoloRift && matches(itemName, currentSoloColl)) {
                matchedAny = true
                val maxGain = maxGainForCollection(currentSoloColl)

                if (gainCount in 1..maxGain) {
                    soloGainToProcess += gainCount.toLong()
                }
            }

            for (coll in multiRiftCollections) {
                if (matches(itemName, coll)) {
                    matchedAny = true
                    val maxGain = maxGainForCollection(coll)

                    if (gainCount in 1..maxGain) {
                        multiGains[coll] = (multiGains[coll] ?: 0L) + gainCount
                    }
                }
            }

            if (matchedAny) {
                slotMatchHits[i] = 0
                lastInventoryState[i] = currentCount
            } else {
                val hits = (slotMatchHits[i] ?: 0) + 1
                if (hits >= 5) {
                    slotSkipMap[i] = now + 60_000L // skip for 1 min
                    slotMatchHits[i] = 0
                } else {
                    slotMatchHits[i] = hits
                }
                lastInventoryState[i] = 0
            }
        }

        if (soloGainToProcess > 0) {
            TrackingRates.calculateRates(soloGainToProcess)
        }
        if (multiGains.isNotEmpty()) {
            MultiTrackingRates.calculateMultiRates(multiGains)
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

        lines.firstOrNull()?.let {
            pendingConsumable = HandItemState(
                hand = hand,
                itemName = it,
                count = stack.count,
                slot = player.inventory.selected,
                timestamp = System.currentTimeMillis()
            )
        }

        return InteractionResult.PASS
    }

    private fun matches(itemName: String, collection: String): Boolean {
        return itemName.startsWith(collection)
    }

    private fun maxGainForCollection(collection: String): Int =
        when (collection) {
            "half-eaten carrot" -> 8 // suppose max threshold
            else -> 1
        }

    private fun checkIfConsumableConsumed(client: Minecraft) =
        pendingConsumable?.let { pending ->
            val now = System.currentTimeMillis()

            if (now - pending.timestamp > 1000L) {
                pendingConsumable = null
                return@let
            }

            val player = client.player ?: return@let
            val currentStack = player.inventory.getItem(pending.slot)

            if (currentStack.isEmpty || currentStack.count < pending.count) {
                TemporaryBuffsParser.resetConsumable(pending.itemName)
                pendingConsumable = null
            }
        }


    private data class HandItemState(
        val hand: InteractionHand,
        val itemName: String,
        val count: Int,
        val slot: Int,
        val timestamp: Long
    )
}