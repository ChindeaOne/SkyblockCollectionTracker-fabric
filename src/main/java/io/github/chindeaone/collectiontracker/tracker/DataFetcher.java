package io.github.chindeaone.collectiontracker.tracker;

import io.github.chindeaone.collectiontracker.api.hypixelapi.HypixelApiFetcher;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.util.PlayerData;
import io.github.chindeaone.collectiontracker.util.ServerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.chindeaone.collectiontracker.commands.StartTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.isPaused;
import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.isTracking;

public class DataFetcher {

    private static final Logger logger = LogManager.getLogger(DataFetcher.class);
    private static final Map<CacheKey, String> collectionCache = new HashMap<>();
    private static final Map<CacheKey, Long> cacheTimestamps = new HashMap<>();
    private static final long CACHE_LIFESPAN = 150000; // 2.5 minutes
    public static ScheduledExecutorService scheduler;

    public static void scheduleCollectionDataFetch() {
        int period = 180; // Default
        List<String> timeConsumingCollections = Arrays.asList("cropie", "squash", "refined mineral", "glossy gemstone");

        if (timeConsumingCollections.contains(collection)) {
            period = 600;
        }
        scheduler.scheduleAtFixedRate(DataFetcher::fetchData, 1, period, TimeUnit.SECONDS);
        logger.info("[SCT]: Data fetching scheduled to run every 180 seconds");
    }

    public static void fetchData() {
        try {
            if (!ServerUtils.INSTANCE.getServerStatus()) {
                logger.warn("[SCT]: API server not online. Stopping the tracker.");
                TrackingHandlerClass.stopTracking();
                return;
            }

            if (!isTracking) return;
            if (isPaused) return;

            String playerUUID = PlayerData.INSTANCE.getPlayerUUID();
            String jsonData = getData(playerUUID, collection);

            if (jsonData == null) {
                logger.error("[SCT]: Failed to fetch or retrieve data from the cache");
                return;
            }

            logger.info("[SCT]: Data successfully fetched or retrieved and displayed for player with UUID: {} and collection: {}", playerUUID, collection);
            TrackingRates.calculateRates(jsonData);

        } catch (Exception e) {
            logger.error("[SCT]: Error fetching data from the Hypixel API: {}", e.getMessage(), e);
        }
    }

    public static String getData(String playerUUID, String collection) {
        CacheKey cacheKey = new CacheKey(playerUUID, collection);
        Long lastFetched = cacheTimestamps.get(cacheKey);

        if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < CACHE_LIFESPAN) {
            logger.info("[SCT]: Returning cached data for player with UUID: {} and collection: {}", playerUUID, collection);
            return collectionCache.get(cacheKey);
        }

        logger.info("[SCT]: Cache expired or missing. Fetching new data for player with UUID: {} and collection: {}", playerUUID, collection);
        String jsonData = HypixelApiFetcher.fetchJsonData(playerUUID, TokenManager.getToken(), collection);

        if (jsonData != null) {
            collectionCache.put(cacheKey, jsonData);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
        }
        return jsonData;
    }

    private record CacheKey(String uuid, String collection) {

        @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                CacheKey cacheKey = (CacheKey) o;
                return uuid.equals(cacheKey.uuid) && collection.equals(cacheKey.collection);
            }

    }
}
