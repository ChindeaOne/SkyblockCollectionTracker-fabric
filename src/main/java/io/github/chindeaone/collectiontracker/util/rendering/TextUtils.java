package io.github.chindeaone.collectiontracker.util.rendering;

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices;
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices;
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.ConfigHelper;

import io.github.chindeaone.collectiontracker.config.categories.bazaar.BazaarConfig.BazaarType;
import io.github.chindeaone.collectiontracker.config.categories.overlay.SingleOverlay;
import io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass;
import io.github.chindeaone.collectiontracker.tracker.TrackingRenderData;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import io.github.chindeaone.collectiontracker.util.StringUtils;
import io.github.chindeaone.collectiontracker.util.tab.CommissionsWidget;
import io.github.chindeaone.collectiontracker.util.tab.MiningStatsWidget;
import io.github.chindeaone.collectiontracker.util.world.BlockWatcher;
import io.github.chindeaone.collectiontracker.util.parser.MiningStatsParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.commands.StartTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.*;
import static io.github.chindeaone.collectiontracker.tracker.TrackingRates.*;
import static io.github.chindeaone.collectiontracker.util.NumbersUtils.formatNumber;

public class TextUtils {

    public static List<String> formattedCommissions = new ArrayList<>();
    public static List<String> formattedMiningStats = new ArrayList<>();
    private static boolean hasNpcPrice;

    private static volatile TrackingRenderData RENDER_DATA = TrackingRenderData.EMPTY;

    public static TrackingRenderData getRenderData() {
        return RENDER_DATA;
    }

    public static void rebuildTrackingRenderData() {

        List<String> main = buildMainTrackingLines();
        List<String> extra = buildExtraTrackingLines();

        RENDER_DATA = new TrackingRenderData(
                List.copyOf(main),
                List.copyOf(extra)
        );
    }

    public static List<String> buildMainTrackingLines() {
        List<String> lines = new ArrayList<>();

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        if (!isTracking) {
            return lines;
        }

        if (ConfigAccess.getStatsText().isEmpty()) {
            return lines;
        }

        for (SingleOverlay.OverlayText id : ConfigAccess.getStatsText()) {
            String line = switch (id) {
                case COLLECTION -> handleCollection();
                case COLLECTION_SESSION -> handleCollectionSession();
                case COLL_PER_HOUR -> handleCollectionPerHour();
                case MONEY_PER_HOUR -> handleMoneyPerHour();
                case MONEY_MADE -> handleMoneyMade();
                case COLLECTION_SINCE_LAST -> handleCollectionSinceLast();
            };

            if (line != null) {
                lines.add(line);
            }
        }
        // Always add uptime
        lines.add(uptimeString());
        return lines;
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
        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableBazaar();
            ChatUtils.INSTANCE.sendMessage("§cYou cannot use Bazaar prices for this collection!", true);
            return null;
        }

        if (CollectionsManager.isRiftCollection(collection)) return null;

        hasNpcPrice = NpcPrices.getNpcPrice(collection) != -1;

        if (!ConfigAccess.isUsingBazaar() && hasNpcPrice) {
            return "$/h (NPC): " + formatNumberOrPlaceholder(moneyPerHourNPC);
        }

        long localMoneyPerHour;
        switch (collectionType) {
            case "normal" -> {
                localMoneyPerHour = moneyPerHourBazaar.get(collectionType);
                return "$/h (Bazaar): " + formatNumberOrPlaceholder(localMoneyPerHour);
            }
            case "enchanted" -> {
                if (ConfigAccess.getBazaarType().equals(BazaarType.ENCHANTED_VERSION)) {
                    localMoneyPerHour = moneyPerHourBazaar.get("Enchanted version");
                    return "$/h (Bazaar): " + formatNumberOrPlaceholder(localMoneyPerHour);
                } else {
                    localMoneyPerHour = moneyPerHourBazaar.getOrDefault("Super Enchanted version", -1L);
                    if (localMoneyPerHour == -1L) {
                        ConfigHelper.setBazaarType(BazaarType.ENCHANTED_VERSION);
                        return null;
                    } else return "$/h (Bazaar): " + formatNumberOrPlaceholder(localMoneyPerHour);
                }
            }
            case "gemstone" -> {
                localMoneyPerHour = moneyPerHourBazaar.get(ConfigAccess.getGemstoneVariant().toString());
                return "$/h (Bazaar): " + formatNumberOrPlaceholder(localMoneyPerHour);
            }
            default -> { return null; }
        }
    }

    private static String handleMoneyMade() {
        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableBazaar();
            ChatUtils.INSTANCE.sendMessage("§cYou cannot use Bazaar prices for this collection!", true);
            return null;
        }

        if (CollectionsManager.isRiftCollection(collection)) return null;

        hasNpcPrice = NpcPrices.getNpcPrice(collection) != -1;

        if (!ConfigAccess.isUsingBazaar() && hasNpcPrice) {
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
                if (ConfigAccess.getBazaarType().equals(BazaarType.ENCHANTED_VERSION)) {
                    localMoneyMade = moneyMade.get("Enchanted version");
                    return "$ made (Bazaar): " + formatNumberOrPlaceholder(localMoneyMade);
                } else {
                    localMoneyMade = moneyMade.getOrDefault("Super Enchanted version", -1L);
                    if (localMoneyMade == -1L) {
                        ConfigHelper.setBazaarType(BazaarType.ENCHANTED_VERSION);
                        return null;
                    } else return "$ made (Bazaar): " + formatNumberOrPlaceholder(localMoneyMade);
                }
            }
            case "gemstone" -> {
                localMoneyMade = moneyMade.get(ConfigAccess.getGemstoneVariant().toString());
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

    public static List<String> buildExtraTrackingLines() {
        if (!TrackingHandlerClass.isTracking) return List.of();
        if (!ConfigAccess.isShowExtraStats()) return List.of();
        if (!ConfigAccess.isUsingBazaar()) return List.of();
        if (CollectionsManager.isRiftCollection(collection)) return List.of();
        if (collectionType.equals("normal")) return List.of();
        
        List<String> lines = new ArrayList<>();

        lines.add("§6§lExtra Stats:");
        for (SingleOverlay.OverlayExtraText id : ConfigAccess.getExtraStatsText()) {
            String line = switch (id) {
                case BAZAAR_ITEM -> handleBazaarItem();
                case BAZAAR_PRICE -> handleBazaarPrice();
            };

            if (line != null) {
                lines.add(line);
            }
        }

        return lines;
    }

    private static String handleBazaarItem() {
        switch (collectionType) {
            // Skip normal items
            case "enchanted" -> {
                if (ConfigAccess.getBazaarType().equals(BazaarType.ENCHANTED_VERSION)) {
                    return "Bazaar item: " + StringUtils.formatBazaarItemName(BazaarCollectionsManager.enchantedRecipe.keySet().iterator().next());
                } else {
                    if (BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                        ConfigHelper.setBazaarType(BazaarType.ENCHANTED_VERSION);
                        return null;
                    } else return "Bazaar item: " + StringUtils.formatBazaarItemName(BazaarCollectionsManager.superEnchantedRecipe.keySet().iterator().next());
                }
            }
            case "gemstone" -> {
                return "Bazaar variant: " + ConfigAccess.getGemstoneVariant();
            }
            default -> { return null; }
        }
    }

    private static String handleBazaarPrice() {
        switch (collectionType) {
            // Skip normal items
            case "enchanted" -> {
                if (ConfigAccess.getBazaarType().equals(BazaarType.ENCHANTED_VERSION)) {
                    return  "Item price: " + formatNumber((long) BazaarPrices.enchantedPrice);
                } else {
                    if (BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                        ConfigHelper.setBazaarType(BazaarType.ENCHANTED_VERSION);
                        return null;
                    } else return  "Item price: " + formatNumber((long) BazaarPrices.superEnchantedPrice);
                }
            }

            case "gemstone" -> {
                return "Variant price: " + formatNumber((long) GemstonePrices.getPrice(ConfigAccess.getGemstoneVariant().toString()));
            }

            default -> { return null; }
        }
    }



    public static List<String> updateCommissions() {
        List<String> raw = CommissionsWidget.INSTANCE.getRawCommissions();
        if (raw.isEmpty()) return Collections.emptyList();

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
        if (raw.isEmpty()) return Collections.emptyList();

        formattedMiningStats.clear();
        formattedMiningStats.addAll(MiningStatsParser.parse(raw, BlockWatcher.INSTANCE.getMiningBlockType()));
        return formattedMiningStats;
    }

    public static String uptimeString() {
        return ("Uptime: " + getUptime());
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