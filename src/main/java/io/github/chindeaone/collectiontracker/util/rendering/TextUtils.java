package io.github.chindeaone.collectiontracker.util.rendering;

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices;
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices;
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices;
import io.github.chindeaone.collectiontracker.config.ModConfig;
import io.github.chindeaone.collectiontracker.config.categories.bazaar.BazaarConfig;
import io.github.chindeaone.collectiontracker.config.categories.bazaar.BazaarConfig.BazaarType;
import io.github.chindeaone.collectiontracker.config.categories.overlay.SingleOverlay;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import io.github.chindeaone.collectiontracker.util.StringUtils;
import io.github.chindeaone.collectiontracker.util.tab.CommissionsWidget;
import io.github.chindeaone.collectiontracker.util.tab.MiningStatsWidget;
import io.github.chindeaone.collectiontracker.util.world.BlockWatcher;
import io.github.chindeaone.collectiontracker.util.mining.MiningStatsParser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.commands.StartTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.*;
import static io.github.chindeaone.collectiontracker.tracker.TrackingRates.*;
import static io.github.chindeaone.collectiontracker.util.NumbersUtils.formatNumber;

public class TextUtils {

    public static List<String> formattedCommissions = new ArrayList<>();
    public static List<String> formattedMiningStats = new ArrayList<>();
    private final static List<String> overlayLines = new ArrayList<>();
    private final static List<String> extraOverlayLines = new ArrayList<>();
    private static boolean hasNpcPrice;

    private static ModConfig config = Objects.requireNonNull(SkyblockCollectionTracker.configManager.getConfig());
    private static BazaarConfig bazaarConfig;
    private static BazaarType bazaarType;
    private static SingleOverlay singleOverlay;

    public static void updateStats() {
        checkConfig();

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        if (!isTracking) {
            overlayLines.clear();
            return;
        }

        overlayLines.clear();
        if (singleOverlay == null || singleOverlay.statsText == null) return;

        for (SingleOverlay.OverlayText id : singleOverlay.statsText) {
            switch (id) {
                case COLLECTION -> addIfNotNull(handleCollection());
                case COLLECTION_SESSION -> addIfNotNull(handleCollectionSession());
                case COLL_PER_HOUR -> addIfNotNull(handleCollectionPerHour());
                case MONEY_PER_HOUR -> addIfNotNull(handleMoneyPerHour());
                case MONEY_MADE -> addIfNotNull(handleMoneyMade());
                case COLLECTION_SINCE_LAST -> addIfNotNull(handleCollectionSinceLast());
            }
        }
    }

    private static void addIfNotNull(String line) {
        if (line != null) overlayLines.add(line);
    }

    private static String handleCollection() {
        if (CollectionsManager.collectionSource.equals("sacks")) return null;
        return collectionAmount >=0
                ? formatCollectionName(collection) + " collection: " + formatNumber(collectionAmount)
                : formatCollectionName(collection) + " collection: Calculating...";
    }

    private static String handleCollectionSession() {
        return collectionMade > 0
                ? formatCollectionName(collection) + " collection (session): " + formatNumber(collectionMade)
                : formatCollectionName(collection) + " collection (session): Calculating...";
    }

    private static String handleCollectionPerHour() {
        return collectionPerHour > 0
                ? "Coll/h: " + formatNumber(collectionPerHour)
                : "Coll/h: Calculating...";
    }

    private static String handleMoneyPerHour() {
        if (!BazaarCollectionsManager.hasBazaarData && bazaarConfig.useBazaar) {
            config.getBazaar().bazaarConfig.useBazaar = false;
            ChatUtils.INSTANCE.sendMessage("§cYou cannot use Bazaar prices for this collection!", true);
            return null;
        }

        if (CollectionsManager.isRiftCollection(collection)) return null;

        hasNpcPrice = NpcPrices.getNpcPrice(collection) != -1;

        if (!bazaarConfig.useBazaar && hasNpcPrice) {
            return "$/h (NPC): " + formatNumberOrPlaceholder(moneyPerHourNPC);
        }

        long localMoneyPerHour;
        switch (collectionType) {
            case "normal" -> {
                localMoneyPerHour = moneyPerHourBazaar.get(collectionType);
                return "$/h (Bazaar): " + formatNumberOrPlaceholder(localMoneyPerHour);
            }
            case "enchanted" -> {
                if (bazaarType.equals(BazaarType.ENCHANTED_VERSION)) {
                    localMoneyPerHour = moneyPerHourBazaar.get("Enchanted version");
                    return "$/h (Bazaar): " + formatNumberOrPlaceholder(localMoneyPerHour);
                } else {
                    localMoneyPerHour = moneyPerHourBazaar.getOrDefault("Super Enchanted version", -1L);
                    if (localMoneyPerHour == -1L) {
                        config.getBazaar().bazaarConfig.bazaarType = BazaarType.ENCHANTED_VERSION;
                        return null;
                    } else return "$/h (Bazaar): " + formatNumberOrPlaceholder(localMoneyPerHour);
                }
            }
            case "gemstone" -> {
                localMoneyPerHour = moneyPerHourBazaar.get(bazaarConfig.gemstoneVariant.toString());
                return "$/h (Bazaar): " + formatNumberOrPlaceholder(localMoneyPerHour);
            }
            default -> { return null; }
        }
    }

    private static String handleMoneyMade() {
        if (!BazaarCollectionsManager.hasBazaarData && bazaarConfig.useBazaar) {
            config.getBazaar().bazaarConfig.useBazaar = false;
            ChatUtils.INSTANCE.sendMessage("§cYou cannot use Bazaar prices for this collection!", true);
            return null;
        }

        if (CollectionsManager.isRiftCollection(collection)) return null;

        hasNpcPrice = NpcPrices.getNpcPrice(collection) != -1;

        if (!bazaarConfig.useBazaar && hasNpcPrice) {
            long moneyMadeNPC = moneyMade.get("NPC");
            return "$ made (NPC): " + formatNumberOrPlaceholder(moneyMadeNPC);
        }

        long localMoneyMade;
        switch (collectionType) {
            case "normal" -> {
                localMoneyMade = moneyMade.get(collectionType);
                return "$ made (Bazaar): " + formatNumberOrPlaceholder(localMoneyMade);
            }
            case "enchanted" -> {
                if (bazaarType.equals(BazaarType.ENCHANTED_VERSION)) {
                    localMoneyMade = moneyMade.get("Enchanted version");
                    return "$ made (Bazaar): " + formatNumberOrPlaceholder(localMoneyMade);
                } else {
                    localMoneyMade = moneyMade.getOrDefault("Super Enchanted version", -1L);
                    if (localMoneyMade == -1L) {
                        config.getBazaar().bazaarConfig.bazaarType = BazaarType.ENCHANTED_VERSION;
                        return null;
                    } else return "$ made (Bazaar): " + formatNumberOrPlaceholder(localMoneyMade);
                }
            }
            case "gemstone" -> {
                localMoneyMade = moneyMade.get(bazaarConfig.gemstoneVariant.toString());
                return"$ made (Bazaar): " + formatNumberOrPlaceholder(localMoneyMade);
            }
            default -> { return null; }
        }
    }

    private static String handleCollectionSinceLast() {
        return collectionSinceLast > 0
                ? formatCollectionName(collection) + " collection since last: " + formatNumber(collectionSinceLast)
                : formatCollectionName(collection) + " collection since last: Calculating...";
    }

    public static void updateExtraStats() {
        checkConfig();

        if (CollectionsManager.isRiftCollection(collection) && singleOverlay.showExtraStats) {
            config.getTrackingOverlay().singleOverlay.showExtraStats = false;
            ChatUtils.INSTANCE.sendMessage("§cExtra stats are not available for Rift collections!", true);
            extraOverlayLines.clear();
            return;
        }

        if (collectionType.equals("normal") && singleOverlay.showExtraStats) {
            config.getTrackingOverlay().singleOverlay.showExtraStats = false;
            ChatUtils.INSTANCE.sendMessage("§cExtra stats are redundant here!", true);
            extraOverlayLines.clear();
            return;
        }

        if (!singleOverlay.showExtraStats || !bazaarConfig.useBazaar) {
            extraOverlayLines.clear();
            return;
        }

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        if (!isTracking) {
            extraOverlayLines.clear();
            return;
        }

        extraOverlayLines.clear();

        extraOverlayLines.add("§6§lExtra Stats:");
        for (SingleOverlay.OverlayExtraText id : singleOverlay.extraStatsText) {
            switch (id) {
                case BAZAAR_ITEM -> addIfNotNullExtra(handleBazaarItem());
                case BAZAAR_PRICE -> addIfNotNullExtra(handleBazaarPrice());
            }
        }
    }

    private static void addIfNotNullExtra(String line) {
        if (line != null) extraOverlayLines.add(line);
    }

    private static String handleBazaarItem() {
        switch (collectionType) {
            // Skip normal items
            case "enchanted" -> {
                if (bazaarType.equals(BazaarType.ENCHANTED_VERSION)) {
                    return "Bazaar item: " + StringUtils.INSTANCE.formatBazaarItemName(BazaarCollectionsManager.enchantedRecipe.keySet().iterator().next());
                } else {
                    if (BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                        config.getBazaar().bazaarConfig.bazaarType = BazaarType.ENCHANTED_VERSION;
                        return null;
                    } else return "Bazaar item: " + StringUtils.INSTANCE.formatBazaarItemName(BazaarCollectionsManager.superEnchantedRecipe.keySet().iterator().next());
                }
            }
            case "gemstone" -> {
                return "Bazaar variant: " + bazaarConfig.gemstoneVariant.toString();
            }
            default -> { return null; }
        }
    }

    private static String handleBazaarPrice() {
        switch (collectionType) {
            // Skip normal items
            case "enchanted" -> {
                if (bazaarType.equals(BazaarType.ENCHANTED_VERSION)) {
                    return  "Item price: " + formatNumber((long) BazaarPrices.enchantedPrice);
                } else {
                    if (BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                        config.getBazaar().bazaarConfig.bazaarType = BazaarType.ENCHANTED_VERSION;
                        return null;
                    } else return  "Item price: " + formatNumber((long) BazaarPrices.superEnchantedPrice);
                }
            }

            case "gemstone" -> {
                return "Variant price: " + formatNumber((long) GemstonePrices.getPrice(bazaarConfig.gemstoneVariant.toString()));
            }

            default -> { return null; }
        }
    }

    public static List<String> updateCommissions() {
        List<String> raw = CommissionsWidget.INSTANCE.getRawCommissions();
        if (raw.isEmpty()) return null;

        formattedCommissions.clear();
        CommissionFormat.Area detectedArea = null;

        for (String line : raw) {
            String formatted = line;
            String lowerLine = line.toLowerCase();
            for (CommissionFormat.CommissionType type : CommissionFormat.INSTANCE.getCOMMISSIONS()) {
                String typeNameLower = type.getName().toLowerCase();
                if (lowerLine.contains(typeNameLower)) {
                    formatted = type.getFormat().invoke(line);
                    if (detectedArea == null) detectedArea = type.getArea();
                    break;
                }
            }
            formattedCommissions.add(formatted);
        }

        if (detectedArea != null) {
            switch (detectedArea) {
                case DWARVEN_MINES -> formattedCommissions.addFirst("§2§l" + detectedArea.getDisplayName());
                case CRYSTAL_HOLLOWS -> formattedCommissions.addFirst("§5§l" + detectedArea.getDisplayName());
                case GLACITE_TUNNELS -> formattedCommissions.addFirst("§b§l" + detectedArea.getDisplayName());
            }
        }
        return formattedCommissions;
    }

    public static List<String> updateMiningStats() {
        List<String> raw = MiningStatsWidget.INSTANCE.getRawStats();
        if (raw.isEmpty()) return null;

        formattedMiningStats.clear();
        formattedMiningStats.addAll(MiningStatsParser.parse(raw, BlockWatcher.INSTANCE.getMiningBlockType()));
        return formattedMiningStats;
    }

    public static @NotNull List<String> getStrings() {
        updateStats();
        if (overlayLines.isEmpty()) return overlayLines;
        List<String> lines = new ArrayList<>(overlayLines);
        lines.add(uptimeString());
        return lines;
    }

    public static @NotNull List<String> getExtraStrings() {
        updateExtraStats();
        return extraOverlayLines;
    }

    public static String uptimeString() {
        return ("Uptime: " + getUptime());
    }

    private static void checkConfig() {
        bazaarConfig = config.getBazaar().bazaarConfig;
        bazaarType = config.getBazaar().bazaarConfig.bazaarType;
        singleOverlay = config.getTrackingOverlay().singleOverlay;
    }

    public static String formatCollectionName(String collection) {
        String[] words = collection.split("\\s+");
        StringBuilder formattedName = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == 0) {
                formattedName.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
            } else {
                formattedName.append(" ").append(word.toLowerCase());
            }
        }
        return formattedName.toString();
    }

    private static String formatNumberOrPlaceholder(long value) {
        return value > 0 ? formatNumber(value) : "Calculating...";
    }
}