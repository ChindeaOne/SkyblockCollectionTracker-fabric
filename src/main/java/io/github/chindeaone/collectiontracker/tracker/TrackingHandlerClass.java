package io.github.chindeaone.collectiontracker.tracker;

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.commands.StartTracker;
import io.github.chindeaone.collectiontracker.config.ModConfig;
import io.github.chindeaone.collectiontracker.config.categories.bazaar.BazaarConfig.BazaarType;
import io.github.chindeaone.collectiontracker.gui.overlays.CollectionOverlay;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import io.github.chindeaone.collectiontracker.util.Hypixel;
import io.github.chindeaone.collectiontracker.util.PlayerData;
import io.github.chindeaone.collectiontracker.util.rendering.TextUtils;
import io.github.chindeaone.collectiontracker.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.tracker.DataFetcher.scheduler;
import static io.github.chindeaone.collectiontracker.tracker.TrackingRates.*;
import static io.github.chindeaone.collectiontracker.util.NumbersUtils.compactFloat;

public class TrackingHandlerClass {

    private static final Logger logger = LogManager.getLogger(TrackingHandlerClass.class);
    private static final long COOLDOWN_MILLIS = TimeUnit.SECONDS.toMillis(15); // 15 seconds cooldown

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
            ChatUtils.INSTANCE.sendMessage("§aTracking " + StartTracker.collection + " collection.", true);
        }

        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }

        lastTrackTime = now;
        isTracking = true;
        isPaused = false;

        CollectionOverlay.setVisible(true);

        startTime = 0;
        lastTime = 0;

        if (!BazaarCollectionsManager.hasBazaarData && Objects.requireNonNull(SkyblockCollectionTracker.configManager.getConfig()).getBazaar().bazaarConfig.useBazaar) {
            SkyblockCollectionTracker.configManager.getConfig().getBazaar().bazaarConfig.useBazaar = false;
            ChatUtils.INSTANCE.sendMessage("§eWarning! Bazaar data not available for " + StartTracker.collection + ". Using NPC prices instead.", true);
        }

        logger.info("[SCT]: Tracking started for player: {}", PlayerData.INSTANCE.getPlayerName());

        DataFetcher.scheduleCollectionDataFetch();
    }

    public static void stopTrackingManual() {
        if (scheduler != null && !scheduler.isShutdown()) {
            ChatUtils.INSTANCE.sendMessage("§cStopped tracking!", true);

            resetTrackingData(false);

            logger.info("[SCT]: Tracking stopped.");

        } else {
            ChatUtils.INSTANCE.sendMessage("§cNo tracking active!", true);
            logger.warn("[SCT]: Attempted to stop tracking, but no tracking is active.");
        }
    }

    public static void stopTracking() {
        if (scheduler != null && !scheduler.isShutdown()) {

            resetTrackingData(false);

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
        if (Objects.requireNonNull(SkyblockCollectionTracker.configManager.getConfig()).getTrackingOverlay().showTrackingRatesAtEndOfSession) sendRates();

        StartTracker.previousCollection = StartTracker.collection;
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

        CollectionOverlay.stopTracking();
    }

    private static void resetLowestHighestRates() {
        highestCollectionPerHour = 0;
        lowestCollectionPerHour = Float.MAX_VALUE;
        highestRatePerHourNPC = 0;
        lowestRatePerHourNPC = Float.MAX_VALUE;
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
        assert StartTracker.collection != null;
        String collectionDisplay = TextUtils.formatCollectionName(StartTracker.collection);

        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add(String.format("   §aCollection tracked: §f%s", collectionDisplay));
        lines.add(String.format("   §b%s Made: §f%s   §bRate: §f%s/h", collectionDisplay, compactFloat(collectionMade), compactFloat(collectionPerHour)));

        if (CollectionsManager.isRiftCollection(StartTracker.collection)) {
            lines.add(String.format("   §7Elapsed time: §f%s", getUptimeInWords()));
            ChatUtils.INSTANCE.sendSummary("Tracking Summary", lines, "§e§l", "§6§l", "§6§m", '=', 10);
            return;
        }

        ModConfig config = SkyblockCollectionTracker.configManager.getConfig();
        assert config != null;

        boolean useBazaar = config.getBazaar().bazaarConfig.useBazaar;
        BazaarType bazaarType = config.getBazaar().bazaarConfig.bazaarType;
        if (!useBazaar) {
            float npcMoney = moneyMade.get("NPC");
            lines.add(String.format("   §6Money (NPC): §f$%s   §6Rate: §f$%s/h", compactFloat(npcMoney), compactFloat(moneyPerHourNPC)));
        } else {
            switch (collectionType) {
                case "normal" -> {
                    float bazMoney = moneyMade.get(collectionType);
                    float bazRate = moneyPerHourBazaar.get(collectionType);
                    lines.add(String.format("   §6Money (Bazaar): §f$%s   §6Rate: §f$%s/h", compactFloat(bazMoney), compactFloat(bazRate)));
                }
                case "enchanted" -> {
                    String key = SkyblockCollectionTracker.configManager.getConfig().getBazaar().bazaarConfig.bazaarType.equals(io.github.chindeaone.collectiontracker.config.categories.bazaar.BazaarConfig.BazaarType.ENCHANTED_VERSION)
                            ? "Enchanted version" : "Super Enchanted version";
                    float money = moneyMade.get(key);
                    float rate = moneyPerHourBazaar.get(key);
                    lines.add(String.format("   §6Money (Bazaar): §f$%s  §6Rate: §f$%s/h", compactFloat(money), compactFloat(rate)));
                }
                case "gemstone" -> {
                    String variant = SkyblockCollectionTracker.configManager.getConfig().getBazaar().bazaarConfig.gemstoneVariant.toString();
                    float gMoney = moneyMade.get(variant);
                    float gRate = moneyPerHourBazaar.get(variant);
                    lines.add(String.format("   §6Money (Bazaar): §f$%s  §6Rate: §f$%s/h", compactFloat(gMoney), compactFloat(gRate)));
                }
            }
        }

        lines.add(String.format("   §7Elapsed time: §f%s", getUptimeInWords()));

        lines.add("");
        lines.add("   §eBest/Worst Rates:");
        lines.add("");

        // Collection extremes
        if (highestCollectionPerHour > 0) {
            lines.add(String.format("   §6Best collection rate: §f%s coll/h", compactFloat(highestCollectionPerHour)));
        }
        if (lowestCollectionPerHour > 0 && lowestCollectionPerHour < Float.MAX_VALUE) {
            lines.add(String.format("   §6Lowest collection rate: §f%s coll/h", compactFloat(lowestCollectionPerHour)));
        }

        if (!useBazaar) {
            // NPC money extremes
            if (highestRatePerHourNPC > 0) {
                lines.add(String.format("   §6Best NPC money rate: §f$%s/h", compactFloat(highestRatePerHourNPC)));
            }
            if (lowestRatePerHourNPC > 0 && lowestRatePerHourNPC < Float.MAX_VALUE) {
                lines.add(String.format("   §6Lowest NPC money rate: §f$%s/h", compactFloat(lowestRatePerHourNPC)));
            }
        } else {
            // Bazaar extremes per variant
            if (!moneyPerHourBazaar.isEmpty()) {
                switch (collectionType) {
                    case "normal" -> {
                        Float low = lowestRatesPerHourBazaar.get("normal");
                        Float high = highestRatesPerHourBazaar.get("normal");

                        float lowD = (low == null || low <= 0f) ? 0.0f : low;
                        float highD = (high == null || high <= 0f) ? 0.0f : high;

                        lines.add(String.format("   §6Best: §f$%s/h", compactFloat(highD)) );
                        lines.add(String.format("   §6Worst: §f$%s/h", compactFloat(lowD)) );
                    }
                    case "enchanted" -> {
                        if (bazaarType.equals(BazaarType.ENCHANTED_VERSION)) {
                            Float low = lowestRatesPerHourBazaar.get("Enchanted version");
                            Float high = highestRatesPerHourBazaar.get("Enchanted version");

                            float lowD = (low == null || low <= 0f) ? 0.0f : low;
                            float highD = (high == null || high <= 0f) ? 0.0f : high;

                            lines.add(String.format("   §6Best: §f$%s/h", compactFloat(highD)) );
                            lines.add(String.format("   §6Worst: §f$%s/h", compactFloat(lowD)) );
                            lines.add(String.format("   §aItem used for Bazaar pricing: §f%s", StringUtils.INSTANCE.formatBazaarItemName(BazaarCollectionsManager.enchantedRecipe.keySet().iterator().next())));
                        } else {
                            Float low = lowestRatesPerHourBazaar.get("Super Enchanted version");
                            Float high = highestRatesPerHourBazaar.get("Super Enchanted version");

                            float lowD = (low == null || low <= 0f) ? 0.0f : low;
                            float highD = (high == null || high <= 0f) ? 0.0f : high;

                            lines.add(String.format("   §6Best Rate: §f$%s/h", compactFloat(highD)));
                            lines.add(String.format("   §6Worst Rate: §f$%s/h", compactFloat(lowD)));
                            lines.add(String.format("   §aItem used for Bazaar pricing: §f%s", StringUtils.INSTANCE.formatBazaarItemName(BazaarCollectionsManager.superEnchantedRecipe.keySet().iterator().next())));
                        }
                    }
                    case "gemstone" -> {
                        String variant = SkyblockCollectionTracker.configManager.getConfig().getBazaar().bazaarConfig.gemstoneVariant.toString();
                        Float low = lowestRatesPerHourBazaar.get(variant);
                        Float high = highestRatesPerHourBazaar.get(variant);

                        float lowD = (low == null || low <= 0f) ? 0.0f : low;
                        float highD = (high == null || high <= 0f) ? 0.0f : high;

                        lines.add(String.format("   §6Best: §f$%s/h", compactFloat(highD)));
                        lines.add(String.format("   §6Worst: §f$%s/h", compactFloat(lowD)));
                        lines.add(String.format("   §aVariant used for Bazaar pricing: §f%s", config.getBazaar().bazaarConfig.gemstoneVariant.toString()));
                    }
                }
            }
        }

        ChatUtils.INSTANCE.sendSummary("Tracking Summary", lines, "§e§l", "§6§l", "§6§m", '=', 10);
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

