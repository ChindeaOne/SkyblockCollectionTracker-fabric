package io.github.chindeaone.collectiontracker.tracker.collection;

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.categories.Bazaar;
import io.github.chindeaone.collectiontracker.config.categories.Bazaar.BazaarType;
import io.github.chindeaone.collectiontracker.gui.OverlayManager;
import io.github.chindeaone.collectiontracker.gui.overlays.CollectionOverlay;
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import io.github.chindeaone.collectiontracker.utils.Hypixel;
import io.github.chindeaone.collectiontracker.utils.PlayerData;
import io.github.chindeaone.collectiontracker.utils.rendering.TextUtils;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.*;
import static io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatNumber;

public class TrackingHandler {

    private static final Logger logger = LogManager.getLogger(TrackingHandler.class);
    public static final long COOLDOWN_MILLIS = TimeUnit.SECONDS.toMillis(10); // 10 seconds cooldown

    public static volatile boolean isTracking = false;
    public static boolean isPaused = false;

    public static long startTime;
    private static long lastTime;
    public static long lastTrackTime = 0;

    private static final int allowedHourlyRestarts = 10;
    private static int restartCount = 0;
    private static long firstRestartTime;

    public static void startTracking() {
        logger.info("[SCT]: Tracking started for player: {}", PlayerData.INSTANCE.getPlayerName());

        OverlayManager.setTrackingOverlayRendering(true);
        DataFetcher.fetchData(true);
    }

    public static void initTracking(long now) {
        lastTrackTime = now;

        isTracking = true;
        isPaused = false;

        startTime = now;
        lastTime = 0;
    }

    public static void stopTrackingManual() {
        if (isTracking) {
            ChatUtils.sendMessage("§cStopped tracking!", true);

            resetTrackingData(false);

            logger.info("[SCT]: Tracking stopped.");

        } else {
            ChatUtils.sendMessage("§cNo tracking active!", true);
            logger.warn("[SCT]: Attempted to stop tracking manually, but no tracking is active.");
        }
    }

    public static void stopTracking() {
        if (isTracking) {

            if (!Hypixel.INSTANCE.getServer()) {
                logger.info("[SCT]: Tracking stopped because player disconnected from the server.");
            } else {
                ChatUtils.sendMessage("§cAPI server is down. Stopping the tracker.", true);
                logger.info("[SCT]: Tracking stopped because the API server is down.");
            }

            resetTrackingData(false);
        } else {
            logger.warn("[SCT]: Attempted to stop tracking, but no tracking is active.");
        }
    }

    public static void restartTracking() {
        if (!isTracking) {
            ChatUtils.sendMessage("§cNo tracking active to restart!", true);
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
            ChatUtils.sendMessage("§cHourly restart limit reached! Cannot restart tracking.", true);
            logger.warn("[SCT]: Hourly restart limit reached. Cannot restart tracking.");
            return;
        }

        restartCount++;
        resetTrackingData(true);
        startTracking();
    }

    private static void resetTrackingData(boolean restart) {
        if (ConfigAccess.isShowTrackingRatesAtEndOfSession()) sendRates();

        resetVariables();
        // Clear cached data
        DataFetcher.clearAllCache();

        // Reset uptime
        long now = System.currentTimeMillis();
        if (!restart) {
            lastTrackTime = now;
            clearFetchedData();
        }
        else lastTrackTime = now - COOLDOWN_MILLIS;

        OverlayManager.setTrackingOverlayRendering(false);
        CollectionOverlay.trackingDirty = false;
    }

    private static void clearFetchedData() {
        CollectionsManager.resetCollections();
        BazaarCollectionsManager.resetBazaarData();
    }

    private static void resetVariables() {
        isTracking = false;
        isPaused = false;
        startTime = 0;
        lastTime = 0;

        // Reset collection tracking
        lastApiCollection = -1L;
        sacksCollectionGained = 0L;
        sessionStartCollection = -1L;
        lastCollectionTime = -1L;

        // Clear profit map
        moneyPerHourBazaar.clear();
        moneyMade.clear();

        // Reset highest/lowest rates
        resetLowestHighestRates();
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
        if (isTracking) {
            if (isPaused) {
                ChatUtils.sendMessage("§cTracking is already paused!", true);
                logger.warn("[SCT]: Attempted to pause tracking, but tracking is already paused.");
                return;
            }
            isPaused = true;
            lastTime += (System.currentTimeMillis() - startTime) / 1000;
            ChatUtils.sendMessage("§7Tracking paused.", true);
            logger.info("[SCT]: Tracking paused.");
        } else {
            ChatUtils.sendMessage("§cNo tracking active!", true);
            logger.warn("[SCT]: Attempted to pause tracking, but no tracking is active.");
        }
    }

    public static void resumeTracking() {
        if (!isTracking) {
            ChatUtils.sendMessage("§cNo tracking active!", true);
            logger.warn("[SCT]: Attempted to resume tracking, but no tracking is active.");
            return;
        }

        if (isTracking && isPaused) {
            ChatUtils.sendMessage("§7Resuming tracking.", true);
            logger.info("[SCT]: Resuming tracking.");
            startTime = System.currentTimeMillis();
            isPaused = false;
        } else if (!isPaused) {
            ChatUtils.sendMessage("§cTracking is already active!", true);
            logger.warn("[SCT]: Attempted to resume tracking, but tracking is already active.");
        } else {
            ChatUtils.sendMessage("§cTracking has not been started yet!", true);
            logger.warn("[SCT]: Attempted to resume tracking, but tracking has not been started.");
        }
    }

    private static void sendRates() {
        assert collection != null;
        String collectionDisplay = TextUtils.formatCollectionName(collection);

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(String.format("   §aCollection tracked: §f%s", collectionDisplay)));
        lines.add(Component.literal(String.format("   §b%s Made: §f%s   §bRate: §f%s/h", collectionDisplay, formatNumber(collectionMade), formatNumber(collectionPerHour))));

        boolean useBazaar = ConfigAccess.isUsingBazaar();
        BazaarType bazaarType = ConfigAccess.getBazaarType();

        if (!useBazaar) {
            long npcMoney = moneyMade.get("NPC");
            if (CollectionsManager.isRiftCollection(collection) && NpcPrices.getNpcPrice(collection) != 0) {
                lines.add(Component.literal(String.format("   §6Motes: §f$%s   §6Rate: §f%s/h", formatNumber(npcMoney), formatNumber(moneyPerHourNPC))));
            } else if (NpcPrices.getNpcPrice(collection) != 0) lines.add(Component.literal(String.format("   §6Money (NPC): §f$%s   §6Rate: §f$%s/h", formatNumber(npcMoney), formatNumber(moneyPerHourNPC))));
        } else {
            String suffix = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? "_INSTANT_BUY" : "_INSTANT_SELL";
            switch (collectionType) {
                case "normal" -> {
                    long bazMoney = moneyMade.getOrDefault(collectionType + suffix, 0L);
                    long bazRate = moneyPerHourBazaar.getOrDefault(collectionType + suffix, 0L);
                    lines.add(Component.literal(String.format("   §6Money (Bazaar): §f$%s   §6Rate: §f$%s/h", formatNumber(bazMoney), formatNumber(bazRate))));
                }
                case "enchanted" -> {
                    String key = bazaarType.equals(BazaarType.ENCHANTED_VERSION)
                            ? "Enchanted version" : "Super Enchanted version";
                    long money = moneyMade.getOrDefault(key + suffix, 0L);
                    long rate = moneyPerHourBazaar.getOrDefault(key + suffix, 0L);
                    lines.add(Component.literal(String.format("   §6Money (Bazaar): §f$%s  §6Rate: §f$%s/h", formatNumber(money), formatNumber(rate))));
                }
                case "gemstone" -> {
                    String variant = ConfigAccess.getGemstoneVariant().toString();
                    long gMoney = moneyMade.getOrDefault(variant + suffix, 0L);
                    long gRate = moneyPerHourBazaar.getOrDefault(variant + suffix, 0L);
                    lines.add(Component.literal(String.format("   §6Money (Bazaar): §f$%s  §6Rate: §f$%s/h", formatNumber(gMoney), formatNumber(gRate))));
                }
            }
        }

        lines.add(Component.literal(String.format("   §7Elapsed time: §f%s", getUptimeInWords())));

        // If no collection update, skip best/worst rates
        if (collectionMade == 0) {
            ChatUtils.INSTANCE.sendSummary("§e§lTracking Summary", lines);
            return;
        }

        lines.add(Component.empty());
        lines.add(Component.literal("   §eBest/Worst Rates:"));
        lines.add(Component.empty());

        // Collection extremes
        if (highestCollectionPerHour > 0) {
            lines.add(Component.literal(String.format("   §6Best collection rate: §f%s coll/h", formatNumber(highestCollectionPerHour))));
        }
        if (lowestCollectionPerHour > 0 && lowestCollectionPerHour < Long.MAX_VALUE) {
            lines.add(Component.literal(String.format("   §6Lowest collection rate: §f%s coll/h", formatNumber(lowestCollectionPerHour))));
        }

        if (!useBazaar) {
            // NPC money extremes
            if (highestRatePerHourNPC > 0) {
                if (CollectionsManager.isRiftCollection(collection) && NpcPrices.getNpcPrice(collection) != 0) {
                    lines.add(Component.literal(String.format("   §6Best motes rate: §f%s/h", formatNumber(highestRatePerHourNPC))));
                } else if (NpcPrices.getNpcPrice(collection) != 0) lines.add(Component.literal(String.format("   §6Best NPC money rate: §f$%s/h", formatNumber(highestRatePerHourNPC))));
            }
            if (lowestRatePerHourNPC > 0 && lowestRatePerHourNPC < Long.MAX_VALUE) {
                if (CollectionsManager.isRiftCollection(collection) && NpcPrices.getNpcPrice(collection) != 0) {
                    lines.add(Component.literal(String.format("   §6Lowest motes rate: §f%s/h", formatNumber(lowestRatePerHourNPC))));
                } else if(NpcPrices.getNpcPrice(collection) != 0) lines.add(Component.literal(String.format("   §6Lowest NPC money rate: §f$%s/h", formatNumber(lowestRatePerHourNPC))));
            }
        } else {
            // Bazaar extremes per variant
            if (!moneyPerHourBazaar.isEmpty()) {
                String suffix = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? "_INSTANT_BUY" : "_INSTANT_SELL";
                switch (collectionType) {
                    case "normal" -> {
                        long low = lowestRatesPerHourBazaar.getOrDefault("normal" + suffix, 0L);
                        long high = highestRatesPerHourBazaar.getOrDefault("normal" + suffix, 0L);

                        lines.add(Component.literal(String.format("   §6Best: §f$%s/h", formatNumber(high))));
                        lines.add(Component.literal(String.format("   §6Worst: §f$%s/h", formatNumber(low))));
                    }
                    case "enchanted" -> {
                        String key = bazaarType.equals(BazaarType.ENCHANTED_VERSION)
                                ? "Enchanted version" : "Super Enchanted version";
                        long low = lowestRatesPerHourBazaar.getOrDefault(key + suffix, 0L);
                        long high = highestRatesPerHourBazaar.getOrDefault(key + suffix, 0L);

                        lines.add(Component.literal(String.format("   §6Best: §f$%s/h", formatNumber(high))));
                        lines.add(Component.literal(String.format("   §6Worst: §f$%s/h", formatNumber(low))));
                    }
                    case "gemstone" -> {
                        String variant = ConfigAccess.getGemstoneVariant().toString();
                        long low = lowestRatesPerHourBazaar.getOrDefault(variant + suffix, 0L);
                        long high = highestRatesPerHourBazaar.getOrDefault(variant + suffix, 0L);

                        lines.add(Component.literal(String.format("   §6Best: §f$%s/h", formatNumber(high))));
                        lines.add(Component.literal(String.format("   §6Worst: §f$%s/h", formatNumber(low))));
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
