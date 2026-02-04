package io.github.chindeaone.collectiontracker.tracker.skills;

import io.github.chindeaone.collectiontracker.api.hypixelapi.SkillApiFetcher;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.util.PlayerData;
import io.github.chindeaone.collectiontracker.util.ServerUtils;
import io.github.chindeaone.collectiontracker.util.SkillUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.isPaused;
import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.isTracking;

public class SkillFetcher {

    public static final Logger logger = LogManager.getLogger(SkillFetcher.class);
    private static final Map<CacheKey, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private static final long CACHE_LIFESPAN_MS = 180_000L; // default 3 minutes
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
                TrackingHandler.stopTracking();
                return;
            }

            if (!isTracking) return;
            if (isPaused) return;


            getData(PlayerData.INSTANCE.getPlayerUUID(), skillName); // fetch data for the tracked skill
            Double skillXp = SkillUtils.getSkillValue(skillName); // get the XP of the tracked skill again here

            if (!isSkillMaxed) SkillTrackingRates.calculateSkillRates(skillXp != null ? skillXp.longValue() : 0L); // only if skill isn't maxed, as maxed skills use chat messages to track
            SkillTrackingRates.calculateTamingRates(SkillUtils.getTamingValue().longValue());
        } catch (Exception e) {
            logger.error("[SCT]: Error while fetching data from the Hypixel API", e);
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
        logger.info("[SCT]: All skill data caches have been cleared.");
    }

    private record CacheKey(String uuid, String skill) { }
}