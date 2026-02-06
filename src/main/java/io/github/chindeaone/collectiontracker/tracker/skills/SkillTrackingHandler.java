package io.github.chindeaone.collectiontracker.tracker.skills;

import io.github.chindeaone.collectiontracker.commands.SkillTracker;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.gui.OverlayManager;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import io.github.chindeaone.collectiontracker.util.Hypixel;
import io.github.chindeaone.collectiontracker.util.SkillUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.github.chindeaone.collectiontracker.commands.SkillTracker.skillName;
import static io.github.chindeaone.collectiontracker.tracker.skills.SkillFetcher.scheduler;
import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingRates.afk;

public class SkillTrackingHandler {

    private static final Logger logger = LogManager.getLogger(SkillTrackingHandler.class);

    public static boolean isTracking = false;
    public static boolean isPaused = false;

    public static long startTime;
    private static long lastTime;
    private static long lastTrackedTime = 0;
    private static final long TRACKING_INTERVAL = TimeUnit.SECONDS.toMillis(10); // 10 seconds

    private static final int allowedHourlyRestarts = 10;
    private static int restartCount = 0;
    private static long firstRestartTime;

    public static boolean isSkillMaxed = false;

    public static void startTracking() {
        long now = System.currentTimeMillis();

        if (now - lastTrackedTime < TRACKING_INTERVAL) {
            ChatUtils.INSTANCE.sendMessage("§cPlease wait a few seconds before tracking another skill!", true);
            return;
        } else {
            ChatUtils.INSTANCE.sendMessage("§aTracking " + skillName + " skill.", true);
        }

        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }

        initTracking(now);
        OverlayManager.setSkillOverlayRendering(true);

        isSkillMaxed = Boolean.TRUE.equals(SkillUtils.isSkillMaxed(skillName));
        Integer skillLevel = SkillUtils.getSkillLevel(skillName);
        Double skillXp = SkillUtils.getSkillValue(skillName);

        SkillTrackingRates.initTracking(skillLevel != null ? skillLevel : 0, skillXp != null ? skillXp.longValue() : 0L);

        if (!isSkillMaxed || ConfigAccess.isTamingTrackingEnabled()) {
            // Track only via API
            SkillFetcher.scheduleSkillFetch(isSkillMaxed, skillXp != null ? skillXp.longValue() : 0L, skillName);
        }
        logger.info("[SCT]: Started tracking skill: {}", skillName);
    }

    public static void onSkillGain(long value, String skillName) {
        if (!isTracking || !SkillTracker.skillName.equals(skillName) || !isSkillMaxed) return;
        SkillTrackingRates.calculateSkillRates(value);
    }

    private static void initTracking(long now) {
        lastTrackedTime = now;

        isTracking = true;
        isPaused = false;

        startTime = now;
        lastTime = 0;
    }

    public static void pauseTracking() {
        if (checkTracking()) return;
        if (isPaused) {
            ChatUtils.INSTANCE.sendMessage("§cSkill tracking is already paused.", true);
            logger.warn("[SCT]: Skills tracking is already paused.");
            return;
        }
        isPaused = true;
        lastTime = (System.currentTimeMillis() - startTime) / 1000;
        ChatUtils.INSTANCE.sendMessage("§7Paused tracking " + skillName.toLowerCase() + " skill.", true);
        logger.info("[SCT]: Pausing tracking skill: {}", skillName);
    }

    public static void resumeTracking() {
        if (checkTracking()) return;
        if (!isPaused) {
            ChatUtils.INSTANCE.sendMessage("§cSkill tracking is not paused.", true);
            logger.warn("[SCT]: Skills tracking is not paused.");
            return;
        }
        isPaused = false;
        startTime = System.currentTimeMillis();
        ChatUtils.INSTANCE.sendMessage("§7Resumed tracking " + skillName.toLowerCase() + " skill.", true);
        logger.info("[SCT]: Resuming tracking skill: {}", skillName);
    }

    public static void stopTracking() {
        if (checkTracking()) return;

        if (!Hypixel.INSTANCE.getServer()) {
            logger.info("[SCT]: Tracking stopped because player disconnected from the server.");
        } else if (afk) {
            ChatUtils.INSTANCE.sendMessage("§cYou have been marked as AFK. Stopping the tracker.", true);
            logger.info("[SCT]: Tracking stopped because the player went AFK or the API server is down");
            } else {
                ChatUtils.INSTANCE.sendMessage("§cAPI server is down. Stopping the skill tracker.", true);
                logger.info("[SCT]: Skill tracking stopped because the API server is down.");
            }

        resetTrackingData(false);
    }

    public static void stopTrackingManual() {
        if (checkTracking()) return;

        resetTrackingData(false);

        ChatUtils.INSTANCE.sendMessage("§cStopped tracking " + skillName.toLowerCase() + " skill!", true);
        logger.info("[SCT]: Stopped tracking skill: {}", skillName);
    }

    private static void resetTrackingData(boolean restart) {
        if (scheduler != null) {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        isTracking = false;
        isPaused = false;

        startTime = 0;
        lastTime = 0;

        long now = System.currentTimeMillis();
        if (!restart) {
            lastTrackedTime = now;
        } else lastTrackedTime = now - TRACKING_INTERVAL;

        isSkillMaxed = false;
        OverlayManager.setSkillOverlayRendering(false);

        SkillFetcher.clearCache();
        SkillTrackingRates.resetSession();
    }

    private static boolean checkTracking() {
        if (!isTracking) {
            ChatUtils.INSTANCE.sendMessage("§cNo skill is being tracked currently!", true);
            logger.warn("[SCT]: No skill is being tracked currently.");
            return true;
        }
        return false;
    }

    public static void restartTracking() {
        if (checkTracking()) return;

        if (restartCount == 0) {
            firstRestartTime = System.currentTimeMillis();
        } else {
            long elapsedTime = System.currentTimeMillis() - firstRestartTime;
            if (elapsedTime >= TimeUnit.HOURS.toMillis(1)) {
                restartCount = 0;
                firstRestartTime = System.currentTimeMillis();
            }
        }

        if (restartCount >= allowedHourlyRestarts) {
            ChatUtils.INSTANCE.sendMessage("§cHourly restart limit reached. Please wait before restarting again.", true);
            logger.warn("[SCT]: Hourly restart limit reached for skill tracking.");
            return;
        }

        restartCount++;
        resetTrackingData(true);
        startTracking();
    }

    public static long getUptimeInSeconds() {
        if (startTime == 0) {
            return 0;
        }

        if (isPaused) {
            return lastTime;
        } else {
            return lastTime + (System.currentTimeMillis() - startTime) / 1000;
        }
    }

    public static String getUptime() {
        if (startTime == 0) return "00:00:00";

        long uptime;

        if (isPaused) {
            uptime = lastTime;
        } else {
            uptime = lastTime + (System.currentTimeMillis() - startTime) / 1000;
        }

        long hours = uptime / 3600;
        long minutes = (uptime % 3600) / 60;
        long seconds = uptime % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
