package io.github.chindeaone.collectiontracker.tracker.collection;

import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.api.hypixelapi.HypixelApiFetcher;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.util.PlayerData;
import io.github.chindeaone.collectiontracker.util.ServerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler.isPaused;
import static io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler.isTracking;

public class DataFetcher {

    private static final Logger logger = LogManager.getLogger(DataFetcher.class);
    private static final Map<CacheKey, String> collectionCache = new ConcurrentHashMap<>();
    private static final Map<CacheKey, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_LIFESPAN_MS = 180_000L; // default 3 minutes
    public static ScheduledExecutorService scheduler;

    public static void scheduleCollectionDataFetch() {
        int period = 200; // Default (3 minutes 20 seconds)
        List<String> timeConsumingCollections = Arrays.asList("cropie", "squash", "refined mineral", "glossy gemstone");

        if (timeConsumingCollections.contains(collection)) {
            period = 600;
        }

        scheduler.scheduleAtFixedRate(DataFetcher::fetchData, 1, period, TimeUnit.SECONDS);
        logger.info("[SCT]: Data fetching scheduled to run every {} seconds", period);
    }

    private static void fetchData() {
        try {
            if (!ServerUtils.INSTANCE.getServerStatus()) {
                logger.warn("[SCT]: API server not online. Stopping the tracker.");
                TrackingHandler.stopTracking();
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

            long collectionData = JsonParser.parseString(jsonData).getAsJsonObject().entrySet().iterator().next().getValue().getAsLong();

            logger.info("[SCT]: Data successfully fetched or retrieved and displayed for player with UUID: {} and collection: {}", playerUUID, collection);
            TrackingRates.calculateRates(collectionData, false);

        } catch (Exception e) {
            logger.error("[SCT]: Error fetching data from the Hypixel API: {}", e.getMessage(), e);
        }
    }

    private static String getData(String playerUUID, String collection) {
        CacheKey cacheKey = new CacheKey(playerUUID, collection);
        Long lastFetched = cacheTimestamps.get(cacheKey);

        if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < CACHE_LIFESPAN_MS) {
            long elapsed = System.currentTimeMillis() - lastFetched;
            logger.info("[SCT]: Returning cached data for player with UUID: {} and collection: {} (last fetched {} ms ago)", playerUUID, collection, elapsed);
            return collectionCache.get(cacheKey);
        }

        if (lastFetched != null) {
            long elapsed = System.currentTimeMillis() - lastFetched;
            logger.info("[SCT]: Cache expired for player {} collection {} (last fetched {} ms ago). Fetching new data.", playerUUID, collection, elapsed);
        } else {
            logger.info("[SCT]: No cache present for player {} collection {}. Fetching data.", playerUUID, collection);
        }

        String jsonData = HypixelApiFetcher.fetchJsonData(playerUUID, TokenManager.getToken(), collection);

        if (jsonData != null) {
            collectionCache.put(cacheKey, jsonData);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
        }
        return jsonData;
    }

    public static void clearAllCache() {
        collectionCache.clear();
        cacheTimestamps.clear();
        logger.info("[SCT]: All collection data caches have been cleared.");
    }

    private record CacheKey(String uuid, String collection) { }
}