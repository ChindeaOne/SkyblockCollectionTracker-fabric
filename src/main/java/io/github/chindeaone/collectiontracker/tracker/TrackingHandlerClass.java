package io.github.chindeaone.collectiontracker.tracker;

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.gui.overlays.CollectionOverlay;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import io.github.chindeaone.collectiontracker.util.Hypixel;
import io.github.chindeaone.collectiontracker.util.PlayerData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.github.chindeaone.collectiontracker.commands.StartTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.DataFetcher.scheduler;
import static io.github.chindeaone.collectiontracker.tracker.TrackingRates.*;

public class TrackingHandlerClass {

    private static final Logger logger = LogManager.getLogger(TrackingHandlerClass.class);
    private static final int COOLDOWN_PERIOD = 15;
    public static boolean isTracking = false;
    public static boolean isPaused = false;
    public static long startTime;
    public static long lastTime;
    private static long lastTrackTime = 0;

    public static void startTracking() {
        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastTrackTime) / 1000 < COOLDOWN_PERIOD) {
            ChatUtils.INSTANCE.sendMessage("§cPlease wait before tracking another collection!", true);
            return;
        } else {
            ChatUtils.INSTANCE.sendMessage("§aTracking " + collection + " collection.", true);
        }

        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }

        lastTrackTime = currentTime;
        isTracking = true;
        isPaused = false;

        CollectionOverlay.setVisible(true);

        startTime = 0;
        lastTime = 0;

        if (!BazaarCollectionsManager.hasBazaarData && Objects.requireNonNull(SkyblockCollectionTracker.configManager.getConfig()).getBazaar().bazaarConfig.useBazaar) {
            SkyblockCollectionTracker.configManager.getConfig().getBazaar().bazaarConfig.useBazaar = false;
            ChatUtils.INSTANCE.sendMessage("§eWarning! Bazaar data not available for " + collection + ". Using NPC prices instead.", true);
        }

        logger.info("[SCT]: Tracking started for player: {}", PlayerData.INSTANCE.getPlayerName());

        DataFetcher.scheduleCollectionDataFetch();
    }

    public static void stopTrackingManual() {
        if (scheduler != null && !scheduler.isShutdown()) {
            resetTrackingData();

            ChatUtils.INSTANCE.sendMessage("§cStopped tracking!", true);
            logger.info("[SCT]: Tracking stopped.");

        } else {
            ChatUtils.INSTANCE.sendMessage("§cNo tracking active!", true);
            logger.warn("[SCT]: Attempted to stop tracking, but no tracking is active.");
        }
    }

    public static void stopTracking() {
        if (scheduler != null && !scheduler.isShutdown()) {
            resetTrackingData();

            if (!Hypixel.INSTANCE.getServer()) {
                logger.info("[SCT]: Tracking stopped because player disconnected from the server.");
            } else if (afk) {
                logger.info("[SCT]: Tracking stopped because the player went AFK or the API server is down");
            }
            afk = false;

        } else {
            logger.warn("[SCT]: Attempted to stop tracking, but no tracking is active.");
        }
    }

    private static void resetTrackingData() {
        isTracking = false;
        isPaused = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Reset uptime
        lastTrackTime = System.currentTimeMillis();
        startTime = 0;
        lastTime = 0;
        // Reset collection tracking
        previousCollection = -1L;
        sessionStartCollection = -1L;
        // Clear profit map
        moneyPerHourBazaar.clear();
        moneyMade.clear();

        CollectionOverlay.stopTracking();
    }

    public static void pauseTracking() {
        if (scheduler != null && !scheduler.isShutdown()) {
            if (isPaused) {
                ChatUtils.INSTANCE.sendMessage("§cTracking is already paused!", true);
                logger.warn("[SCT]: Attempted to pause tracking, but tracking is already paused.");
                return;
            }
            isPaused = true;
            lastTime += (System.currentTimeMillis() - startTime) / 1000;
            ChatUtils.INSTANCE.sendMessage("§7Tracking paused.", true);
            logger.info("[SCT]: Tracking paused.");
        } else {
            ChatUtils.INSTANCE.sendMessage("§cNo tracking active!", true);
            logger.warn("[SCT]: Attempted to pause tracking, but no tracking is active.");
        }
    }

    public static void resumeTracking() {
        if (scheduler == null || scheduler.isShutdown() && !isTracking) {
            ChatUtils.INSTANCE.sendMessage("§cNo tracking active!", true);
            logger.warn("[SCT]: Attempted to resume tracking, but no tracking is active.");
            return;
        }

        if (isTracking && isPaused) {
            ChatUtils.INSTANCE.sendMessage("§7Resuming tracking.", true);
            logger.info("[SCT]: Resuming tracking.");
            startTime = System.currentTimeMillis();
            isPaused = false;
        } else if (isTracking) {
            ChatUtils.INSTANCE.sendMessage("§cTracking is already active!", true);
            logger.warn("[SCT]: Attempted to resume tracking, but tracking is already active.");
        } else {
            ChatUtils.INSTANCE.sendMessage("§cTracking has not been started yet!", true);
            logger.warn("[SCT]: Attempted to resume tracking, but tracking has not been started.");
        }
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