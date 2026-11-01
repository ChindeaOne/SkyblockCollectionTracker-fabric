package io.github.chindeaone.collectiontracker.commands

import io.github.chindeaone.collectiontracker.api.bazaarapi.FetchBazaarPrice
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.tracker.collection.DataFetcher
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import kotlin.time.Duration.Companion.seconds

object CollectionTracker {

    var collection = ""
    var collectionList = mutableListOf<String>()
    var isApiTracking = false
    var scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    var trackingTask: ScheduledFuture<*>? = null

    var riftTracking = false

    private val logger: Logger = LogManager.getLogger(CollectionTracker::class.java)

    fun startTracking(coll: String) {
        try {
            if (MultiTrackingHandler.isMultiTracking) {
                ChatUtils.sendMessage("§cCannot track solo collections while multi-tracking.")
                return
            }

            if (TrackingHandler.isTracking) {
                ChatUtils.sendMessage("§cAlready tracking a collection.")
                return
            }

            collection = coll.lowercase()

            if (!CollectionsManager.isValidCollection(collection)) {
                ChatUtils.sendMessage("§4$collection collection is not supported! Use `/sct collections` to see all supported collections.")
                return
            }

            // Check for rift collections
            val isRift = CollectionsManager.isRiftCollection(collection)

            if (isRift && !IslandTracker.isInRift) {
                ChatUtils.sendMessage("§cYou must be in The Rift to track rift collections!")
                return
            }

            if (!isRift && IslandTracker.isInRift) {
                ChatUtils.sendMessage("§cYou cannot track non-rift collections while in The Rift!")
                return
            }

            riftTracking = isRift

            // Set collection source
            if (CollectionsManager.isCollection(collection)) {
                CollectionsManager.collectionSource = "collection"
            } else CollectionsManager.collectionSource = "sacks"

            // Check cooldown before fetching bazaar prices
            val lastTrack = TrackingHandler.lastTrackTime
            if (lastTrack != null && lastTrack.timePassedSince() < 10.seconds) {
                ChatUtils.sendMessage("§cPlease wait before tracking another collection!")
                return
            } else {
                ChatUtils.sendMessage("§aTracking $collection collection.")
                if (collection == "timite" || collection == "youngite" || collection == "obsolite") {
                    ChatUtils.sendMessage(" §8Note: Tracking $collection only tracks that item. For the full Timite collection use `/sct track youngite, timite, obsolite`, or track the variant(s) you mine.", false)
                }
            }

            // Fetch bazaar data and leaderboard data asynchronously
            FetchBazaarPrice.fetchData(collection)
                    .thenCompose { DataFetcher.fetchLeaderboardData(collection) }
                    .thenRun(TrackingHandler::startTracking)
        } catch (e: Exception) {
            ChatUtils.sendMessage("§cAn error occurred while processing the command.")
            logger.error("[SCT]: Error processing command: ", e)
        }
    }

    fun startMultiTracking(list: MutableList<String>) {
        try {
            if (TrackingHandler.isTracking) {
                ChatUtils.sendMessage("§cCannot multi-track collections while tracking a collection solo.")
                return
            }

            if (MultiTrackingHandler.isMultiTracking) {
                ChatUtils.sendMessage("§cAlready multi-tracking collections.")
                return
            }

            if (list.isEmpty()) {
                ChatUtils.sendMessage("§cNo valid collections provided!")
                return
            }

            // Validate all collections and build a new list
            val validCollections = mutableListOf<String>()
            for (collection in list) {
                val coll = collection.lowercase().trim()
                if (CollectionsManager.isCollection(coll)) {
                    CollectionsManager.multiCollectionSource.add("collection")
                } else CollectionsManager.multiCollectionSource.add("sacks")

                if (!validCollections.contains(coll)) validCollections.add(coll)
            }
            collectionList = validCollections
            // move gemstone to the end
            if (collectionList.contains("gemstone")) {
                collectionList.remove("gemstone")
                collectionList.add("gemstone")
            }

            // Check for rift collections
            val hasRift = CollectionsManager.isAnyRiftTracked(collectionList)
            val hasNonRift = CollectionsManager.isAnyNonRiftTracked(collectionList)

            if (hasRift && !IslandTracker.isInRift) {
                ChatUtils.sendMessage("§cYou must be in The Rift to track rift collections!")
                return
            }

            if (hasNonRift && IslandTracker.isInRift) {
                ChatUtils.sendMessage("§cYou cannot track non-rift collections while in The Rift!")
                return
            }

            riftTracking = hasRift

            val lastMultiTrack = MultiTrackingHandler.multiLastTrackTime
            if (lastMultiTrack != null && lastMultiTrack.timePassedSince() < 10.seconds) {
                ChatUtils.sendMessage("§cPlease wait before another multi-tracking!")
                return
            } else {
                ChatUtils.sendMessage("§aMulti-tracking " + collectionList.joinToString(", ") + " collections.")
            }

            // Fetch bazaar data asynchronously
            FetchBazaarPrice.fetchData(collectionList)
                    .thenCompose {
                        if (collectionList.size == 1 && collectionList.contains("gemstone")) {
                            val collection = "gemstone"
                            DataFetcher.fetchLeaderboardData(collection)
                        } else CompletableFuture.completedFuture(null)
                    }
                    .thenRun(MultiTrackingHandler::startMultiTracking)
        } catch (e: Exception) {
            ChatUtils.sendMessage("§cAn error occurred while processing the command.")
            logger.error("[SCT]: Error processing command: ", e)
        }
    }

    fun cancelScheduledTask() {
        if (trackingTask != null && !trackingTask!!.isCancelled) {
            trackingTask!!.cancel(true)
            trackingTask = null
        }

        ConfigHelper.setApiTracking(false)
        ChatUtils.sendMessage("§eAPI data could not be fetched. API tracking has been disabled. Continuing with sack tracking.")
    }
}