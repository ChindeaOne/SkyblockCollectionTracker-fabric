package io.github.chindeaone.collectiontracker.commands;

import io.github.chindeaone.collectiontracker.api.bazaarapi.FetchBazaarPrice;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.config.ConfigHelper;
import io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import io.github.chindeaone.collectiontracker.util.HypixelUtils;
import io.github.chindeaone.collectiontracker.util.PlayerData;
import io.github.chindeaone.collectiontracker.util.ServerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.isPaused;
import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.isTracking;

public class StartTracker {

    public static String collection = "";

    public static Logger logger = LogManager.getLogger(StartTracker.class);

    public static void startTracking(String coll) {
        try {
            if (!HypixelUtils.isOnSkyblock()) {
                ChatUtils.INSTANCE.sendMessage("§cYou must be on Hypixel Skyblock to use this command!", true);
                return;
            }
            // Re-enable API server checks if they were permanently disabled
            if (ServerUtils.INSTANCE.getPermanentlyDisabled()) {
                ServerUtils.INSTANCE.clearState();
            }

            try {
                if (!ServerUtils.INSTANCE.getServerStatus()) {
                    ChatUtils.INSTANCE.sendMessage("§cYou can't use any tracking commands at the moment.", true);
                    return;
                }

                if (!isTracking && !isPaused) {
                    collection = coll.toLowerCase();
                    if (!CollectionsManager.isValidCollection(collection)) {
                        ChatUtils.INSTANCE.sendMessage("§4" + collection + " collection is not supported! Use /sct collections to see all supported collections.", true);
                        return;
                    }
                    // Set collection source
                    if (CollectionsManager.isCollection(collection)) {
                        CollectionsManager.collectionSource = "collection";
                    } else CollectionsManager.collectionSource = "sacks";

                    // Fetch bazaar data asynchronously
                    CompletableFuture.runAsync(() -> FetchBazaarPrice.fetchData(PlayerData.INSTANCE.getPlayerUUID(), TokenManager.getToken(), collection))
                            .thenRun(TrackingHandlerClass::startTracking);
                } else {
                    ChatUtils.INSTANCE.sendMessage("§cAlready tracking a collection.", true);
                }

            } catch (Exception e) {
                ChatUtils.INSTANCE.sendMessage("§cAn error occurred while processing the command.", true);
                logger.error("Error processing command: ", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}