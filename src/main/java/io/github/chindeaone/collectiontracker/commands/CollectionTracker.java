package io.github.chindeaone.collectiontracker.commands;

import io.github.chindeaone.collectiontracker.api.bazaarapi.FetchBazaarPrice;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.GemstonesManager;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler;
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
import io.github.chindeaone.collectiontracker.utils.ServerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CollectionTracker {

    public static String collection = "";
    public static List<String> collectionList = new LinkedList<>();

    public static Logger logger = LogManager.getLogger(CollectionTracker.class);

    public static void startTracking(String coll) {
        try {
            if (!HypixelUtils.isOnSkyblock()) {
                ChatUtils.INSTANCE.sendMessage("§cYou must be on Hypixel Skyblock to use this command!", true);
                return;
            }
            try {
                if (!ServerUtils.INSTANCE.getServerStatus()) {
                    ChatUtils.INSTANCE.sendMessage("§cYou can't use any tracking commands at the moment.", true);
                    return;
                }

                if (MultiTrackingHandler.isMultiTracking()) {
                    ChatUtils.INSTANCE.sendMessage("§cCannot track collections normally while multi-tracking.", true);
                    return;
                }

                if (TrackingHandler.isTracking || TrackingHandler.isPaused) {
                    ChatUtils.INSTANCE.sendMessage("§cAlready tracking a collection.", true);
                    return;
                }

                collection = coll.toLowerCase();
                // Remove general gemstone from normal tracking
                if (collection.equals("gemstone")) {
                    ChatUtils.INSTANCE.sendMessage("§cThe `gemstone` collection isn't supported for normal tracking anymore. Use `/sct track-multi gemstone instead`!", true);
                    return;
                }

                if (!CollectionsManager.isValidCollection(collection)) {
                    ChatUtils.INSTANCE.sendMessage("§4" + collection + " collection is not supported! Use `/sct collections` to see all supported collections.", true);
                    return;
                }
                // Set collection source
                if (CollectionsManager.isCollection(collection)) {
                    CollectionsManager.collectionSource = "collection";
                } else CollectionsManager.collectionSource = "sacks";

                // Check cooldown before fetching bazaar prices
                if (System.currentTimeMillis() - TrackingHandler.lastTrackTime < TrackingHandler.COOLDOWN_MILLIS) {
                    ChatUtils.INSTANCE.sendMessage("§cPlease wait before tracking another collection!", true);
                    return;
                } else {
                    ChatUtils.INSTANCE.sendMessage("§aTracking " + collection + " collection.", true);
                }

                // Fetch bazaar data asynchronously
                CompletableFuture.runAsync(() -> FetchBazaarPrice.fetchData(PlayerData.INSTANCE.getPlayerUUID(), TokenManager.getToken(), collection))
                        .thenRun(TrackingHandler::startTracking);
            } catch (Exception e) {
                ChatUtils.INSTANCE.sendMessage("§cAn error occurred while processing the command.", true);
                logger.error("[SCT]: Error processing command: ", e);
            }
        } catch (Exception e) {
            logger.error("[SCT]: Unexpected error when starting tracking: ", e);
        }
    }

    public static void startMultiTracking(List<String> list) {
        try {
            if (!HypixelUtils.isOnSkyblock()) {
                ChatUtils.INSTANCE.sendMessage("§cYou must be on Hypixel Skyblock to use this command!", true);
                return;
            }
            try {
                if (!ServerUtils.INSTANCE.getServerStatus()) {
                    ChatUtils.INSTANCE.sendMessage("§cYou can't use any tracking commands at the moment.", true);
                    return;
                }

                if (TrackingHandler.isTracking) {
                    ChatUtils.INSTANCE.sendMessage("§cCannot multi-track collections while tracking a collection normally.", true);
                    return;
                }

                if (MultiTrackingHandler.isMultiTracking() || MultiTrackingHandler.isMultiPaused()) {
                    ChatUtils.INSTANCE.sendMessage("§cAlready multi-tracking collections.", true);
                    return;
                }

                if (list.isEmpty()) {
                    ChatUtils.INSTANCE.sendMessage("§cNo valid collections provided!", true);
                    return;
                }

                // Validate all collections and build a new list
                List <String> validCollections = new LinkedList<>();
                for (String coll : list) {
                    coll = coll.toLowerCase().trim();
                    if (GemstonesManager.checkIfGemstone(coll)) {
                        ChatUtils.INSTANCE.sendMessage("§cIndividual gemstones aren't supported. Please use `/sct track-multi gemstone`!", true);
                        return;
                    }
                    if (CollectionsManager.isCollection(coll)) {
                        CollectionsManager.multiCollectionSource.add("collection");
                    } else CollectionsManager.multiCollectionSource.add("sacks");

                    if (!validCollections.contains(coll)) validCollections.add(coll);
                }
                collectionList = validCollections;
                // move gemstone to the end
                if (collectionList.contains("gemstone")) {
                    collectionList.remove("gemstone");
                    collectionList.add("gemstone");
                }

                if (System.currentTimeMillis() - MultiTrackingHandler.getMultiLastTrackTime() < MultiTrackingHandler.getCOOLDOWN_MILLIS()) {
                    ChatUtils.INSTANCE.sendMessage("§cPlease wait before another multi-tracking!", true);
                    return;
                } else {
                    ChatUtils.INSTANCE.sendMessage("§aMulti-tracking " + String.join(", ", collectionList) + " collections.", true);
                }

                // Fetch bazaar data asynchronously
                CompletableFuture.runAsync(() -> FetchBazaarPrice.fetchData(PlayerData.INSTANCE.getPlayerUUID(), TokenManager.getToken(), collectionList))
                        .thenRun(MultiTrackingHandler::startMultiTracking);
            } catch (Exception e) {
                ChatUtils.INSTANCE.sendMessage("§cAn error occurred while processing the command.", true);
                logger.error("[SCT]: Error processing command: ", e);
            }

        } catch (Exception e) {
            logger.error("[SCT]: Unexpected error when starting multi-tracking: ", e);
        }
    }
}