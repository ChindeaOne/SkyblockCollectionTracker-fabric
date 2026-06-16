package io.github.chindeaone.collectiontracker.tracker.skills;

import io.github.chindeaone.collectiontracker.api.hypixelapi.SkillApiFetcher;
import io.github.chindeaone.collectiontracker.api.eliteapi.EliteApiFetcher;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
import io.github.chindeaone.collectiontracker.utils.ServerUtils;
import io.github.chindeaone.collectiontracker.utils.SkillUtils;
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager;
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardEntry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.isPaused;
import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.isTracking;

public class SkillFetcher {

    public static final Logger logger = LogManager.getLogger(SkillFetcher.class);
    private static final Map<CacheKey, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final Map<String, Long> leaderboardCacheTimestamps = new ConcurrentHashMap<>();
    private static final Map<String, AtomicBoolean> skillLeaderboardFetchInProgress = new ConcurrentHashMap<>();
    private static final long CACHE_LIFESPAN_MS = 180_000L; // default 3 minutes
    private static final long LEADERBOARD_CACHE_LIFESPAN_MS = 3_600_000L; // 1 hour
    public static ScheduledExecutorService scheduler;

    public static void scheduleSkillFetch(boolean isSkillMaxed, long value, String skillName) {
        int period = 200; // 200 seconds (3 minutes 20 seconds)

        // initial delay of 200s because data is already fetched when tracking starts
        scheduler.scheduleAtFixedRate(() -> fetchSkillData(skillName, isSkillMaxed),200, period, TimeUnit.SECONDS);
        // because of that, manual call is needed
        if (!isSkillMaxed) SkillTrackingRates.calculateSkillRates(value); // only if skill isn't maxed, as maxed skills use chat messages to track
        SkillTrackingRates.calculateTamingRates(SkillUtils.getTamingValue().longValue());
        logger.info("[SCT]: Skill data fetching scheduled to run every {} seconds", period);
    }

    private static void fetchSkillData(String skillName, boolean isSkillMaxed) {
        try {
            if (!ServerUtils.INSTANCE.getServerStatus()) {
                logger.warn("[SCT]: API server not online. Stopping the skill tracker.");
                SkillTrackingHandler.stopTracking();
                return;
            }

            if (!isTracking) return;
            if (isPaused) return;


            getData(PlayerData.INSTANCE.getPlayerUUID(), skillName); // fetch data for the tracked skill

            // Skill leaderboard fetching
            fetchSkillLeaderboardData(skillName);
            if (ConfigAccess.isTamingTrackingEnabled()) {
                fetchSkillLeaderboardData("Taming");
            }

            Double skillXp = SkillUtils.getSkillValue(skillName); // get the XP of the tracked skill again here

            if (!isSkillMaxed) SkillTrackingRates.calculateSkillRates(skillXp != null ? skillXp.longValue() : 0L); // only if skill isn't maxed, as maxed skills use chat messages to track
            SkillTrackingRates.calculateTamingRates(SkillUtils.getTamingValue().longValue());
        } catch (Exception e) {
            logger.error("[SCT]: Error while fetching data from the Hypixel API", e);
        }
    }

    public static void fetchSkillLeaderboardData(String skillName) {
        if (skillName == null || skillName.isEmpty()) return;
        if (!ConfigAccess.isSkillLeaderboardEnabled()) return;

        AtomicBoolean inProgress = skillLeaderboardFetchInProgress.computeIfAbsent(skillName.toLowerCase(), _ -> new AtomicBoolean(false));
        if (!inProgress.compareAndSet(false, true)) return;

        try {
            Long lastFetched = leaderboardCacheTimestamps.get(skillName.toLowerCase());
            if (lastFetched != null && (System.currentTimeMillis() - lastFetched) < LEADERBOARD_CACHE_LIFESPAN_MS) {
                return;
            }
            logger.info("[SCT]: Fetching leaderboard data for skill: {}", skillName);

            String jsonData = EliteApiFetcher.fetchCollectionLeaderboard(skillName.toLowerCase());
            if (jsonData == null) {
                logger.error("[SCT]: Failed to fetch leaderboard data for skill {} from the Elite API", skillName);
                return;
            }

            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            JsonArray entriesArray = jsonObject.getAsJsonArray("entries");
            List<LeaderboardEntry> entries = new ArrayList<>(entriesArray.size());

            for (int i = 0; i < entriesArray.size(); i++) {
                JsonObject entryObject = entriesArray.get(i).getAsJsonObject();
                String username = entryObject.get("username").getAsString();
                if (username.equalsIgnoreCase(PlayerData.INSTANCE.getPlayerName())) continue;

                entries.add(new LeaderboardEntry(
                        username,
                        entryObject.get("rank").getAsInt(),
                        entryObject.get("amount").getAsLong()
                ));
            }

            LeaderboardManager.setSkillLeaderboard(skillName, entries);
            leaderboardCacheTimestamps.put(skillName.toLowerCase(), System.currentTimeMillis());
            logger.info("[SCT]: Leaderboard data successfully fetched and updated for skill: {}", skillName);
        } catch (Exception e) {
            logger.error("[SCT]: Error fetching skill leaderboard data for {}: {}", skillName, e.getMessage(), e);
        } finally {
            inProgress.set(false);
        }
    }

    private static void getData(String playerUUID, String skill) {
        CacheKey cacheKey = new CacheKey(playerUUID, skill);
        long now = System.currentTimeMillis();
        Long lastFetched = cacheTimestamps.get(cacheKey);

        if (lastFetched != null && (now - lastFetched) < CACHE_LIFESPAN_MS) {
            long elapsed = System.currentTimeMillis() - lastFetched;
            logger.info("[SCT]: Using cached data for player {} skill {} (last fetched {} ms ago).", playerUUID, skill, elapsed);
        }

        if (lastFetched != null) {
            long elapsed = now - lastFetched;
            logger.info("[SCT]: Cache expired for player: {} and skill: {} (last fetched {} ms ago). Fetching new data.", playerUUID, skill, elapsed);
        } else {
            logger.info("[SCT]: No cache present for player: {} and skill: {}. Fetching data.", playerUUID, skill);
        }

        SkillApiFetcher.fetchSkillsData();
        cacheTimestamps.put(cacheKey, now);
    }

    public static void clearCache() {
        cacheTimestamps.clear();
        leaderboardCacheTimestamps.clear();
        skillLeaderboardFetchInProgress.clear();
        logger.info("[SCT]: All skill data caches have been cleared.");
    }

    private record CacheKey(String uuid, String skill) { }
}