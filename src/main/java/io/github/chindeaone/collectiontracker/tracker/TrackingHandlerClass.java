package io.github.chindeaone.collectiontracker.tracker;

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.commands.StartTracker;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.ConfigHelper;
import io.github.chindeaone.collectiontracker.config.categories.bazaar.BazaarConfig;
import io.github.chindeaone.collectiontracker.config.categories.bazaar.BazaarConfig.BazaarType;
import io.github.chindeaone.collectiontracker.gui.OverlayManager;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import io.github.chindeaone.collectiontracker.util.Hypixel;
import io.github.chindeaone.collectiontracker.util.PlayerData;
import io.github.chindeaone.collectiontracker.util.rendering.TextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.commands.StartTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.DataFetcher.scheduler;
import static io.github.chindeaone.collectiontracker.tracker.TrackingRates.*;
import static io.github.chindeaone.collectiontracker.util.NumbersUtils.formatNumber;

public class TrackingHandlerClass {

    private static final Logger logger = LogManager.getLogger(TrackingHandlerClass.class);
    private static final long COOLDOWN_MILLIS = TimeUnit.SECONDS.toMillis(10); // 10 seconds cooldown

    public static boolean isTracking = false;
    public static boolean isPaused = false;

    public static long startTime;
    private static long lastTime;
    private static long lastTrackTime = 0;

    private static final int allowedHourlyRestarts = 10;
    private static int restartCount = 0;
    private static long firstRestartTime;

    public static void startTracking() {
        long now = System.currentTimeMillis();

        if ((now - lastTrackTime) < COOLDOWN_MILLIS) {
            ChatUtils.INSTANCE.sendMessage("§cPlease wait before tracking another collection!", true);
            return;
        } else {
            ChatUtils.INSTANCE.sendMessage("§aTracking " + collection + " collection.", true);
        }

        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }

        initTracking(now);
        OverlayManager.setTrackingOverlayRendering(true);

        validateTrackingConfigOnStart();

        logger.info("[SCT]: Tracking started for player: {}", PlayerData.INSTANCE.getPlayerName());

        DataFetcher.scheduleCollectionDataFetch();
    }

    public static void initTracking(long now) {
        lastTrackTime = now;

        isTracking = true;
        isPaused = false;

        startTime = 0;
        lastTime = 0;
    }

    public static void validateTrackingConfigOnStart() {
        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableBazaar();
            ChatUtils.INSTANCE.sendMessage("§eWarning! Bazaar data not available for " + collection + ". Using NPC prices instead.", true);
        }

        if (CollectionsManager.isRiftCollection(collection) && ConfigAccess.isShowExtraStats()) {
            ConfigHelper.disableExtraStats();
            ChatUtils.INSTANCE.sendMessage("§cExtra stats are not available for Rift collections!", true);
        }

        if (collectionType.equals("normal") && ConfigAccess.isShowExtraStats()) {
            ConfigHelper.disableExtraStats();
            ChatUtils.INSTANCE.sendMessage("§cExtra stats are redundant here!", true);
        }
    }

    public static void stopTrackingManual() {
        if (scheduler != null && !scheduler.isShutdown()) {
            ChatUtils.INSTANCE.sendMessage("§cStopped tracking!", true);

            resetTrackingData(false);

            logger.info("[SCT]: Tracking stopped.");

        } else {
            ChatUtils.INSTANCE.sendMessage("§cNo tracking active!", true);
            logger.warn("[SCT]: Attempted to stop tracking manually, but no tracking is active.");
        }
    }

    public static void stopTracking() {
        if (scheduler != null && !scheduler.isShutdown()) {

            resetTrackingData(false);

            if (!Hypixel.INSTANCE.getServer()) {
                logger.info("[SCT]: Tracking stopped because player disconnected from the server.");
            } else if (afk) {
                ChatUtils.INSTANCE.sendMessage("§cYou have been marked as AFK. Stopping the tracker.", true);
                logger.info("[SCT]: Tracking stopped because the player went AFK or the API server is down");
            }
            afk = false;

        } else {
            logger.warn("[SCT]: Attempted to stop tracking, but no tracking is active.");
        }
    }


    public static void restartTracking() {
        if (!isTracking) {
            ChatUtils.INSTANCE.sendMessage("§cNo tracking active to restart!", true);
            logger.warn("[SCT]: Attempted to restart tracking, but no tracking is active.");
            return;
        }

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
            ChatUtils.INSTANCE.sendMessage("§cHourly restart limit reached! Cannot restart tracking.", true);
            logger.warn("[SCT]: Hourly restart limit reached. Cannot restart tracking.");
            return;
        }

        restartCount++;
        resetTrackingData(true);
        startTracking();
    }

    private static void resetTrackingData(boolean restart) {
        if (ConfigAccess.isShowTrackingRatesAtEndOfSession()) sendRates();

        StartTracker.previousCollection = collection;
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
        // Clear cached data
        DataFetcher.clearAllCache();

        // Reset uptime
        long now = System.currentTimeMillis();
        if (!restart) lastTrackTime = now;
        else lastTrackTime = now - COOLDOWN_MILLIS;

        startTime = 0;
        lastTime = 0;
        // Reset collection tracking
        previousCollection = -1L;
        sessionStartCollection = -1L;
        // Clear profit map
        moneyPerHourBazaar.clear();
        moneyMade.clear();
        // Reset highest/lowest rates
        resetLowestHighestRates();

        OverlayManager.setTrackingOverlayRendering(false);
    }

    private static void resetLowestHighestRates() {
        highestCollectionPerHour = 0;
        lowestCollectionPerHour = Long.MAX_VALUE;
        highestRatePerHourNPC = 0;
        lowestRatePerHourNPC = Long.MAX_VALUE;
        lowestRatesPerHourBazaar.clear();
        highestRatesPerHourBazaar.clear();
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

    private static void sendRates() {
        assert collection != null;
        String collectionDisplay = TextUtils.formatCollectionName(collection);

        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add(String.format("   §aCollection tracked: §f%s", collectionDisplay));
        lines.add(String.format("   §b%s Made: §f%s   §bRate: §f%s/h", collectionDisplay, formatNumber(collectionMade), formatNumber(collectionPerHour)));

        if (CollectionsManager.isRiftCollection(collection)) {
            lines.add(String.format("   §7Elapsed time: §f%s", getUptimeInWords()));
            ChatUtils.INSTANCE.sendSummary("§e§lTracking Summary", lines);
            return;
        }

        boolean useBazaar = ConfigAccess.isUsingBazaar();
        BazaarType bazaarType = ConfigAccess.getBazaarType();

        if (!useBazaar) {
            long npcMoney = moneyMade.get("NPC");
            lines.add(String.format("   §6Money (NPC): §f$%s   §6Rate: §f$%s/h", formatNumber(npcMoney), formatNumber(moneyPerHourNPC)));
        } else {
            switch (collectionType) {
                case "normal" -> {
                    long bazMoney = moneyMade.get(collectionType);
                    long bazRate = moneyPerHourBazaar.get(collectionType);
                    lines.add(String.format("   §6Money (Bazaar): §f$%s   §6Rate: §f$%s/h", formatNumber(bazMoney), formatNumber(bazRate)));
                }
                case "enchanted" -> {
                    String key = bazaarType.equals(BazaarConfig.BazaarType.ENCHANTED_VERSION)
                            ? "Enchanted version" : "Super Enchanted version";
                    long money = moneyMade.get(key);
                    long rate = moneyPerHourBazaar.get(key);
                    lines.add(String.format("   §6Money (Bazaar): §f$%s  §6Rate: §f$%s/h", formatNumber(money), formatNumber(rate)));
                }
                case "gemstone" -> {
                    String variant = ConfigAccess.getGemstoneVariant().toString();
                    long gMoney = moneyMade.get(variant);
                    long gRate = moneyPerHourBazaar.get(variant);
                    lines.add(String.format("   §6Money (Bazaar): §f$%s  §6Rate: §f$%s/h", formatNumber(gMoney), formatNumber(gRate)));
                }
            }
        }

        lines.add(String.format("   §7Elapsed time: §f%s", getUptimeInWords()));

        // If no 2nd fetching cycle, skip best/worst rates
        if (getUptimeInSeconds() < 200) {
            ChatUtils.INSTANCE.sendSummary("§e§lTracking Summary", lines);
            return;
        }

        lines.add("");
        lines.add("   §eBest/Worst Rates:");
        lines.add("");

        // Collection extremes
        if (highestCollectionPerHour > 0) {
            lines.add(String.format("   §6Best collection rate: §f%s coll/h", formatNumber(highestCollectionPerHour)));
        }
        if (lowestCollectionPerHour > 0 && lowestCollectionPerHour < Long.MAX_VALUE) {
            lines.add(String.format("   §6Lowest collection rate: §f%s coll/h", formatNumber(lowestCollectionPerHour)));
        }

        if (!useBazaar) {
            // NPC money extremes
            if (highestRatePerHourNPC > 0) {
                lines.add(String.format("   §6Best NPC money rate: §f$%s/h", formatNumber(highestRatePerHourNPC)));
            }
            if (lowestRatePerHourNPC > 0 && lowestRatePerHourNPC < Long.MAX_VALUE) {
                lines.add(String.format("   §6Lowest NPC money rate: §f$%s/h", formatNumber(lowestRatePerHourNPC)));
            }
        } else {
            // Bazaar extremes per variant
            if (!moneyPerHourBazaar.isEmpty()) {
                switch (collectionType) {
                    case "normal" -> {
                        long low = lowestRatesPerHourBazaar.getOrDefault("normal", 0L);
                        long high = highestRatesPerHourBazaar.getOrDefault("normal", 0L);

                        lines.add(String.format("   §6Best: §f$%s/h", formatNumber(high)) );
                        lines.add(String.format("   §6Worst: §f$%s/h", formatNumber(low)) );
                    }
                    case "enchanted" -> {
                        if (bazaarType.equals(BazaarType.ENCHANTED_VERSION)) {
                            long low = lowestRatesPerHourBazaar.getOrDefault("Enchanted version", 0L);
                            long high = highestRatesPerHourBazaar.getOrDefault("Enchanted version", 0L);

                            lines.add(String.format("   §6Best: §f$%s/h", formatNumber(high)) );
                            lines.add(String.format("   §6Worst: §f$%s/h", formatNumber(low)) );
                        } else {
                            long low = lowestRatesPerHourBazaar.getOrDefault("Super Enchanted version", 0L);
                            long high = highestRatesPerHourBazaar.getOrDefault("Super Enchanted version", 0L);

                            lines.add(String.format("   §6Best Rate: §f$%s/h", formatNumber(high)));
                            lines.add(String.format("   §6Worst Rate: §f$%s/h", formatNumber(low)));
                        }
                    }
                    case "gemstone" -> {
                        String variant = ConfigAccess.getGemstoneVariant().toString();
                        long low = lowestRatesPerHourBazaar.getOrDefault(variant, 0L);
                        long high = highestRatesPerHourBazaar.getOrDefault(variant, 0L);

                        lines.add(String.format("   §6Best: §f$%s/h", formatNumber(high)));
                        lines.add(String.format("   §6Worst: §f$%s/h", formatNumber(low)));
                    }
                }
            }
        }

        ChatUtils.INSTANCE.sendSummary("§e§lTracking Summary", lines);
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

    public static String getUptimeInWords() {
        if (startTime == 0) return "0 seconds";

        long uptime = lastTime + (System.currentTimeMillis() - startTime) / 1000;

        long hours = uptime / 3600;
        long minutes = (uptime % 3600) / 60;
        long seconds = uptime % 60;

        return hours > 0
            ? String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds)
            : minutes > 0
                ? String.format("%d minutes, %d seconds", minutes, seconds)
                : String.format("%d seconds", seconds);
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

