package io.github.chindeaone.collectiontracker.tracker.collection;

import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.api.hypixelapi.HypixelApiFetcher;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
import io.github.chindeaone.collectiontracker.utils.ServerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler.isPaused;
import static io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler.isTracking;

public class DataFetcher {

    private static final Logger logger = LogManager.getLogger(DataFetcher.class);
    private static final Map<CacheKey, Long> collectionCache = new ConcurrentHashMap<>();
    private static final Map<CacheKey, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_LIFESPAN_MS = 180_000L; // default 3 minutes

    public static void fetchData() {
        logger.info("[SCT]: Fetching collection data");

        try {
            if (!ServerUtils.INSTANCE.getServerStatus()) {
                logger.warn("[SCT]: API server not online. Stopping the tracker.");
                TrackingHandler.stopTracking();
                return;
            }
            if (!isTracking || isPaused) return;

            String playerUUID = PlayerData.INSTANCE.getPlayerUUID();
            Long collectionData = getCachedData(playerUUID, collection);

            if (collectionData == null) {
                String jsonData = fetchDataFromApi(playerUUID, collection);
                if (jsonData == null) {
                    logger.error("[SCT]: Failed to fetch data from the Hypixel API");
                    TrackingHandler.stopTracking();
                    return;
                }
                collectionData = JsonParser.parseString(jsonData).getAsJsonObject().entrySet().iterator().next().getValue().getAsLong();

                CacheKey cacheKey = new CacheKey(playerUUID, collection);
                collectionCache.put(cacheKey, collectionData);
                cacheTimestamps.put(cacheKey, System.currentTimeMillis());
            }

            logger.info("[SCT]: Data successfully fetched or retrieved for player with UUID: {} and collection: {}", playerUUID, collection);
            TrackingRates.setCollection(collectionData);

        } catch (Exception e) {
            logger.error("[SCT]: Error fetching data from the Hypixel API: {}", e.getMessage(), e);
        }
    }

    private static Long getCachedData(String playerUUID, String collection) {
        CacheKey cacheKey = new CacheKey(playerUUID, collection);
        Long lastFetched = cacheTimestamps.get(cacheKey);

        if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < CACHE_LIFESPAN_MS) {
            long elapsed = System.currentTimeMillis() - lastFetched;
            logger.info("[SCT]: Returning cached data for player with UUID: {} and collection: {} (last fetched {} ms ago)", playerUUID, collection, elapsed);
            return collectionCache.get(cacheKey);
        }
        return null;
    }

    private static String fetchDataFromApi(String playerUUID, String collection) {
        CacheKey cacheKey = new CacheKey(playerUUID, collection);
        Long lastFetched = cacheTimestamps.get(cacheKey);

        if (lastFetched != null) {
            long elapsed = System.currentTimeMillis() - lastFetched;
            logger.info("[SCT]: Cache expired for player {} collection {} (last fetched {} ms ago). Fetching new data.", playerUUID, collection, elapsed);
        } else {
            logger.info("[SCT]: No cache present for player {} collection {}. Fetching data.", playerUUID, collection);
        }

        return HypixelApiFetcher.fetchJsonData(playerUUID, TokenManager.getToken(), collection);
    }

    public static void clearAllCache() {
        collectionCache.clear();
        cacheTimestamps.clear();
        logger.info("[SCT]: All collection data caches have been cleared.");
    }

    private record CacheKey(String uuid, String collection) { }
}