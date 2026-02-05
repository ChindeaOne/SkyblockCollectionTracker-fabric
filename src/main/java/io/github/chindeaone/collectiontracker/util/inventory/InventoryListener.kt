package io.github.chindeaone.collectiontracker.util.inventory

import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.collections.GemstonesManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.util.HypixelUtils
import net.minecraft.client.Minecraft
import java.util.Locale.getDefault

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

            val itemName = normalize(stack.itemName.string)

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

    private fun normalize(name: String): String {
        return name
            .lowercase(getDefault())
            .replace(Regex("[^a-z ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun matchesNormal(itemName: String, collection: String): Boolean {
        return itemName.startsWith(collection)
    }

    private fun matchesGemstone(itemName: String, collection: String): Boolean {
        return itemName.endsWith(" $collection gemstone")
    }
}