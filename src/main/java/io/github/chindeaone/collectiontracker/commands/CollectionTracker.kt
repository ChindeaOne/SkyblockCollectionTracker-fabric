package io.github.chindeaone.collectiontracker.commands

import io.github.chindeaone.collectiontracker.api.bazaarapi.FetchBazaarPrice
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.tracker.collection.DataFetcher
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture

object CollectionTracker {

    @JvmField
    var collection = ""
    @JvmStatic
    var collectionList: MutableList<String> = mutableListOf()
    var isApiTracking = false
    var scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    var trackingTask: ScheduledFuture<*>? = null

    private val logger: Logger = LogManager.getLogger(CollectionTracker::class.java)

    fun startTracking(coll: String) {
        try {
            if (!HypixelUtils.isInSkyblock) {
                ChatUtils.sendMessage("§cYou must be on Hypixel Skyblock to use this command!", true)
                return
            }
            try {
                if (!ServerUtils.serverStatus) {
                    ChatUtils.sendMessage("§cYou can't use any tracking commands at the moment.", true)
                    return
                }

                if (MultiTrackingHandler.isMultiTracking) {
                    ChatUtils.sendMessage("§cCannot track solo collections while multi-tracking.", true)
                    return
                }

                if (TrackingHandler.isTracking) {
                    ChatUtils.sendMessage("§cAlready tracking a collection.", true)
                    return
                }

                collection = coll.lowercase()

                if (!CollectionsManager.isValidCollection(collection)) {
                    ChatUtils.sendMessage("§4$collection collection is not supported! Use `/sct collections` to see all supported collections.", true)
                    return
                }

                // Check for rift collections
                if (CollectionsManager.isRiftCollection(collection) && !IslandTracker.isInRift) {
                    ChatUtils.sendMessage("§cYou must be in The Rift to track rift collections!", true)
                    return
                }

                if (!CollectionsManager.isRiftCollection(collection) && IslandTracker.isInRift) {
                    ChatUtils.sendMessage("§cYou cannot track non-rift collections while in The Rift!", true)
                    return
                }

                // Set collection source
                if (CollectionsManager.isCollection(collection)) {
                    CollectionsManager.collectionSource = "collection"
                } else CollectionsManager.collectionSource = "sacks"

                // Check cooldown before fetching bazaar prices
                if (System.currentTimeMillis() - TrackingHandler.lastTrackTime < TrackingHandler.COOLDOWN_MILLIS) {
                    ChatUtils.sendMessage("§cPlease wait before tracking another collection!", true)
                    return
                } else {
                    ChatUtils.sendMessage("§aTracking $collection collection.", true)
                    if (collection == "timite" || collection == "youngite" || collection == "obsolite") {
                        ChatUtils.sendMessage(" §8Note: Tracking $collection only tracks that item. For the full Timite collection use `/sct track youngite, timite, obsolite`, or track the variant(s) you mine.", false)
                    }
                }

                // Fetch bazaar data and leaderboard data asynchronously
                FetchBazaarPrice.fetchData(collection)
                        .thenRunAsync { DataFetcher.fetchLeaderboardData(collection) }
                        .thenRun(TrackingHandler::startTracking)
            } catch (e: Exception) {
                ChatUtils.sendMessage("§cAn error occurred while processing the command.", true)
                logger.error("[SCT]: Error processing command: ", e)
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Unexpected error when starting tracking: ", e)
        }
    }

    fun startMultiTracking(list: MutableList<String>) {
        try {
            if (!HypixelUtils.isInSkyblock) {
                ChatUtils.sendMessage("§cYou must be on Hypixel Skyblock to use this command!", true)
                return
            }
            try {
                if (!ServerUtils.serverStatus) {
                    ChatUtils.sendMessage("§cYou can't use any tracking commands at the moment.", true)
                    return
                }

                if (TrackingHandler.isTracking) {
                    ChatUtils.sendMessage("§cCannot multi-track collections while tracking a collection solo.", true)
                    return
                }

                if (MultiTrackingHandler.isMultiTracking) {
                    ChatUtils.sendMessage("§cAlready multi-tracking collections.", true)
                    return
                }

                if (list.isEmpty()) {
                    ChatUtils.sendMessage("§cNo valid collections provided!", true)
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
                if (CollectionsManager.hasAnyRiftCollection() && !IslandTracker.isInRift) {
                    ChatUtils.sendMessage("§cYou must be in The Rift to track rift collections!", true)
                    return
                }

                if (!CollectionsManager.hasAllRiftCollections() && IslandTracker.isInRift) {
                    ChatUtils.sendMessage("§cYou cannot track non-rift collections while in The Rift!", true)
                    return
                }

                if (System.currentTimeMillis() - MultiTrackingHandler.multiLastTrackTime < MultiTrackingHandler.COOLDOWN_MILLIS) {
                    ChatUtils.sendMessage("§cPlease wait before another multi-tracking!", true)
                    return
                } else {
                    ChatUtils.sendMessage("§aMulti-tracking " + collectionList.joinToString(", ") + " collections.", true)
                }

                // Fetch bazaar data asynchronously
                FetchBazaarPrice.fetchData(collectionList)
                        .thenRunAsync {
                            if (collectionList.size == 1 && collectionList.contains("gemstone")) {
                                val collection = "gemstone"
                                DataFetcher.fetchLeaderboardData(collection)
                            }
                        }
                        .thenRun(MultiTrackingHandler::startMultiTracking)
            } catch (e: Exception) {
                ChatUtils.sendMessage("§cAn error occurred while processing the command.", true)
                logger.error("[SCT]: Error processing command: ", e)
            }
        } catch (e: Exception) {
            logger.error("[SCT]: Unexpected error when starting multi-tracking: ", e)
        }
    }

    fun cancelScheduledTask() {
        if (trackingTask != null && !trackingTask!!.isCancelled) {
            trackingTask!!.cancel(true)
            trackingTask = null
        }

        ConfigHelper.setApiTracking(false)
        ChatUtils.sendMessage("§eAPI data could not be fetched. Automatic API tracking has been disabled. Continuing with sack tracking.", true)
    }
}