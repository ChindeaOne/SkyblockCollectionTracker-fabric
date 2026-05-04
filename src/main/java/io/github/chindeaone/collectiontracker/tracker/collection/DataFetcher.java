package io.github.chindeaone.collectiontracker.tracker.collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.api.eliteapi.EliteApiFetcher;
import io.github.chindeaone.collectiontracker.api.hypixelapi.HypixelApiFetcher;
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.gui.CustomCollectionScreen;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
import io.github.chindeaone.collectiontracker.utils.ServerUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler.isPaused;
import static io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler.isTracking;

public class DataFetcher {

    private static final Logger logger = LogManager.getLogger(DataFetcher.class);
    private static final Map<String, Long> collectionCache = new ConcurrentHashMap<>();
    private static final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final Map<String, Long> leaderboardCacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_LIFESPAN_MS = 180_000L; // 3 minutes
    private static final long LEADERBOARD_CACHE_LIFESPAN_MS = 3_600_000L; // 1 hour

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void fetchData(boolean isInitialFetch) {
        logger.info("[SCT]: Fetching collection data");

        try {
            if (!ServerUtils.INSTANCE.getServerStatus()) {
                logger.warn("[SCT]: API server not online. Stopping the tracker.");
                TrackingHandler.stopTracking();
                return;
            }
            if (!isInitialFetch && (!isTracking || isPaused)) return;

            String playerUUID = PlayerData.INSTANCE.getPlayerUUID();
            Long collectionData = getCachedData(collection);

            if (collectionData == null) {
                String jsonData = fetchDataFromApi(playerUUID, collection);
                if (jsonData == null) {
                    logger.error("[SCT]: Failed to fetch data from the Hypixel API");
                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new CustomCollectionScreen(List.of(collection))));
                    return;
                }
                collectionData = JsonParser.parseString(jsonData).getAsJsonObject().entrySet().iterator().next().getValue().getAsLong();

                collectionCache.put(collection, collectionData);
                cacheTimestamps.put(collection, System.currentTimeMillis());
            }

            logger.info("[SCT]: Data successfully fetched or retrieved for player with UUID: {} and collection: {}", playerUUID, collection);
            TrackingRates.setCollection(collectionData);

            // Fetch leaderboard data asynchronously with the collection data
            if (ConfigAccess.isLeaderboardTrackingEnabled()) fetchLeaderboardData(false);

        } catch (Exception e) {
            logger.error("[SCT]: Error fetching data from the Hypixel API: {}", e.getMessage(), e);
        }
    }

    private static Long getCachedData(String collection) {
        Long lastFetched = cacheTimestamps.get(collection);

        if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < CACHE_LIFESPAN_MS) {
            long elapsed = System.currentTimeMillis() - lastFetched;
            logger.info("[SCT]: Returning cached data for collection: {} (last fetched {} ms ago)", collection, elapsed);
            return collectionCache.get(collection);
        }
        return null;
    }

    private static String fetchDataFromApi(String playerUUID, String collection) {
        Long lastFetched = cacheTimestamps.get(collection);

        if (lastFetched != null) {
            long elapsed = System.currentTimeMillis() - lastFetched;
            logger.info("[SCT]: Cache expired for player {} collection {} (last fetched {} ms ago). Fetching new data.", playerUUID, collection, elapsed);
        } else {
            logger.info("[SCT]: No cache present for player {} collection {}. Fetching data.", playerUUID, collection);
        }

        return HypixelApiFetcher.fetchJsonData(playerUUID, TokenManager.getToken(), collection);
    }

    public static void clearCollectionCache() {
        collectionCache.clear();
        cacheTimestamps.clear();
        logger.info("[SCT]: Collection data caches have been cleared.");
    }

    public static void clearAllCache() {
        clearCollectionCache();
        leaderboardCacheTimestamps.clear();
        LeaderboardManager.clear();
        logger.info("[SCT]: All data caches, including leaderboard, have been cleared.");
    }

    public static void fetchLeaderboardData(boolean force) {
        executor.execute(() -> {
            try {
                if (!force) {
                    Long lastFetched = leaderboardCacheTimestamps.get(collection);
                    if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < LEADERBOARD_CACHE_LIFESPAN_MS) {
                        return;
                    }
                }

                logger.info("[SCT]: Fetching leaderboard data for collection: {}", collection);
                String jsonData = EliteApiFetcher.fetchCollectionLeaderboard(collection);
                if (jsonData == null) {
                    logger.error("[SCT]: Failed to fetch leaderboard data from the Elite API");
                    ChatUtils.sendMessage("§cFailed to fetch leaderboard data.", true);
                    return;
                }

                JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
                JsonArray entriesArray = jsonObject.getAsJsonArray("entries");
                List<LeaderboardEntry> entries = new ArrayList<>();

                for (int i = 0; i < entriesArray.size(); i++) {
                    JsonObject entryObject = entriesArray.get(i).getAsJsonObject();
                    String username = entryObject.get("username").getAsString();
                    int rank = entryObject.get("rank").getAsInt();
                    long amount = entryObject.get("amount").getAsLong();
                    entries.add(new LeaderboardEntry(username, rank, amount));
                }

                LeaderboardManager.updateLeaderboard(entries);
                leaderboardCacheTimestamps.put(collection, System.currentTimeMillis());
                logger.info("[SCT]: Leaderboard data successfully fetched and updated for collection: {}", collection);

                TrackingRates.updateLeaderboardStats();

            } catch (Exception e) {
                logger.error("[SCT]: Error fetching leaderboard data: {}", e.getMessage(), e);
            }
        });
    }
}