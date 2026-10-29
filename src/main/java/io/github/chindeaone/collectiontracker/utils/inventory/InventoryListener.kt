package io.github.chindeaone.collectiontracker.utils.inventory

import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.commands.CollectionTracker.riftTracking
import io.github.chindeaone.collectiontracker.config.apiTracking
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import net.minecraft.world.entity.item.ItemEntity

object InventoryListener {

    val PICKABLE_COLLECTIONS = setOf("wilted berberis", "caducous stem", "agaricus cap", "half-eaten carrot")

    private data class InventorySnapshot(
        val itemName: String,
        val count: Int
    )

    private val lastInventory = mutableMapOf<Int, InventorySnapshot>()
    private val pendingIgnoredPickups = mutableMapOf<String, Long>()
    private val processedEntityIds = LinkedHashSet<Int>()

    private var wasScreenOpen = false

    private val isTrackingActive: Boolean
        get() = TrackingHandler.isTracking || MultiTrackingHandler.isMultiTracking

    private val isTrackingPaused: Boolean
        get() = TrackingHandler.isPaused || MultiTrackingHandler.isMultiPaused

    fun onClientTick() {
        if (!riftTracking || isTrackingPaused || !IslandTracker.isInRift || apiTracking) return

        if (!isTrackingActive) {
            clearInventoryState()
            return
        }

        if (MinecraftUtils.screen != null) {
            wasScreenOpen = true
            return
        }

        val currentSoloCollection = CollectionTracker.collection
        val isSoloRift = TrackingHandler.isTracking && currentSoloCollection !in PICKABLE_COLLECTIONS

        val multiRiftCollections = if (MultiTrackingHandler.isMultiTracking) {
            CollectionTracker.collectionList.filterNot { it in PICKABLE_COLLECTIONS }
        } else emptyList()

        if (!isSoloRift && multiRiftCollections.isEmpty()) return

        val skipGains = wasScreenOpen || lastInventory.isEmpty()
        wasScreenOpen = false

        val multiGains = mutableMapOf<String, Long>()
        var soloGain = 0L

        for (slot in 0 until 36) {
            val stack = MinecraftUtils.player?.inventory?.getItem(slot) ?: continue

            if (stack.isEmpty) {
                lastInventory.remove(slot)
                continue
            }

            val itemName = normalizeItemName(stack.hoverName.string)
            val currentCount = stack.count
            val previous = lastInventory[slot]

            if (previous != null && previous.itemName != itemName) {
                lastInventory[slot] = InventorySnapshot(itemName, currentCount)
                continue
            }

            val diff = currentCount - (previous?.count ?: 0)

            if (!skipGains && diff > 0) {
                val validGain = consumeIgnoredPickup(itemName, diff.toLong())

                if (validGain == 1L) {
                    if (isSoloRift && itemName.startsWith(currentSoloCollection)) {
                        soloGain++
                    }
                    for (collection in multiRiftCollections) {
                        if (itemName.startsWith(collection)) {
                            multiGains[collection] = (multiGains[collection] ?: 0L) + 1L
                        }
                    }
                }
            }
            lastInventory[slot] = InventorySnapshot(itemName, currentCount)
        }
        pendingIgnoredPickups.clear()

        if (soloGain > 0L) TrackingRates.calculateRates(soloGain)
        if (multiGains.isNotEmpty()) MultiTrackingRates.calculateMultiRates(multiGains)
    }

    @JvmStatic
    fun onItemPickup(entityId: Int, playerId: Int) {
        if (!riftTracking ||!IslandTracker.isInRift || apiTracking) return

        val player = MinecraftUtils.player ?: return
        if (player.id != playerId) return
        if (!isTrackingActive || isTrackingPaused) return

        if (!shouldProcessEntity(entityId)) return

        val level = MinecraftUtils.level ?: return
        val entity = level.getEntity(entityId) as? ItemEntity ?: return
        val stack = entity.item

        if (stack.isEmpty) return

        val itemName = normalizeItemName(stack.hoverName.string)
        val count = stack.count.coerceAtLeast(1).toLong()

        val pickableCollection = PICKABLE_COLLECTIONS.firstOrNull(itemName::startsWith)

        if (pickableCollection != null) {
            processPickableCollection(pickableCollection, count)
        } else {
            notifyNonPickablePickup(itemName, count)
        }
    }

    private fun consumeIgnoredPickup(itemName: String, gain: Long): Long {
        val pending = pendingIgnoredPickups[itemName] ?: return gain
        val ignored = minOf(gain, pending)
        val remaining = pending - ignored

        if (remaining > 0L) {
            pendingIgnoredPickups[itemName] = remaining
        } else {
            pendingIgnoredPickups.remove(itemName)
        }

        return gain - ignored
    }

    private fun shouldProcessEntity(entityId: Int): Boolean {
        if (!processedEntityIds.add(entityId)) return false

        if (processedEntityIds.size > 200) {
            val oldestId = processedEntityIds.iterator().next()
            processedEntityIds.remove(oldestId)
        }

        return true
    }

    private fun processPickableCollection(collection: String, count: Long) {
        if (TrackingHandler.isTracking && CollectionTracker.collection == collection) {
            TrackingRates.calculateRates(count)
        }

        if (MultiTrackingHandler.isMultiTracking && collection in CollectionTracker.collectionList) {
            MultiTrackingRates.calculateMultiRates(mapOf(collection to count))
        }
    }

    private fun normalizeItemName(name: String): String =
        StringUtils.normalizeText(name)

    fun notifyNonPickablePickup(itemName: String, amount: Long) {
        pendingIgnoredPickups[itemName] = (pendingIgnoredPickups[itemName] ?: 0L) + amount
    }

    fun clearInventoryState() {
        wasScreenOpen = false
        lastInventory.clear()
        pendingIgnoredPickups.clear()
        processedEntityIds.clear()
    }
}