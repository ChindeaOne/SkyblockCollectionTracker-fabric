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
import io.github.chindeaone.collectiontracker.util.tab.CommissionsWidget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.commands.StartTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.*;
import static io.github.chindeaone.collectiontracker.tracker.TrackingRates.*;

public class TextUtils {

    public static List<String> formattedCommissions = new ArrayList<>();
    static List<String> overlayLines = new ArrayList<>();
    static List<String> extraOverlayLines = new ArrayList<>();
    static boolean hasNpcPrice;

    static ModConfig config = Objects.requireNonNull(SkyblockCollectionTracker.configManager.getConfig());
    static BazaarConfig bazaarConfig;
    static BazaarType bazaarType;
    static SingleOverlay singleOverlay;

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
        if (CollectionsManager.isRiftCollection(collection)) return null;

        if (!BazaarCollectionsManager.hasBazaarData && bazaarConfig.useBazaar) {
            config.getBazaar().bazaarConfig.useBazaar = false;
            ChatUtils.INSTANCE.sendMessage("§cYou cannot use Bazaar prices for this collection!", true);
            return null;
        }

        hasNpcPrice = NpcPrices.getNpcPrice(collection) != -1;

        if (!bazaarConfig.useBazaar && hasNpcPrice) {
            return "$/h (NPC): " + formatNumberOrPlaceholder(moneyPerHourNPC);
        }

        float localMoneyPerHour;
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
                    localMoneyPerHour = moneyPerHourBazaar.get("Super Enchanted version");
                    if (localMoneyPerHour == -1.0f) {
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
        if (CollectionsManager.isRiftCollection(collection)) return null;

        if (!BazaarCollectionsManager.hasBazaarData && bazaarConfig.useBazaar) {
            config.getBazaar().bazaarConfig.useBazaar = false;
            ChatUtils.INSTANCE.sendMessage("§cYou cannot use Bazaar prices for this collection!", true);
            return null;
        }

        hasNpcPrice = NpcPrices.getNpcPrice(collection) != -1;

        if (!bazaarConfig.useBazaar && hasNpcPrice) {
            float moneyMadeNPC = moneyMade.get("NPC");
            return "$ made (NPC): " + formatNumberOrPlaceholder(moneyMadeNPC);
        }

        float localMoneyMade;
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
                    localMoneyMade = moneyMade.get("Super Enchanted version");
                    if (localMoneyMade == -1.0f) {
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

    public static void updateExtraStats() {
        checkConfig();

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
                case COLLECTION_SINCE_LAST -> addIfNotNullExtra(handleCollectionSinceLast());
            }
        }
    }

    private static void addIfNotNullExtra(String line) {
        if (line != null) extraOverlayLines.add(line);
    }

    private static String handleBazaarItem() {
        switch (collectionType) {
            case "normal" -> {
                return "Bazaar item: " + formatBazaarItemName(collection);
            }

            case "enchanted" -> {
                if (bazaarType.equals(BazaarType.ENCHANTED_VERSION)) {
                    return "Bazaar item: " + formatBazaarItemName(BazaarCollectionsManager.enchantedRecipe.keySet().iterator().next());
                } else {
                    if (BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                        config.getBazaar().bazaarConfig.bazaarType = BazaarType.ENCHANTED_VERSION;
                        return null;
                    } else return "Bazaar item: " + formatBazaarItemName(BazaarCollectionsManager.superEnchantedRecipe.keySet().iterator().next());
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
            case "normal" -> {
                return  "Item price: " + formatNumber(BazaarPrices.normalPrice);
            }

            case "enchanted" -> {
                if (bazaarType.equals(BazaarType.ENCHANTED_VERSION)) {
                    return  "Item price: " + formatNumber(BazaarPrices.enchantedPrice);
                } else {
                    if (BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                        config.getBazaar().bazaarConfig.bazaarType = BazaarType.ENCHANTED_VERSION;
                        return null;
                    } else return  "Item price: " + formatNumber(BazaarPrices.superEnchantedPrice);
                }
            }

            case "gemstone" -> {
                return "Variant price: " + formatNumber(GemstonePrices.getPrice(bazaarConfig.gemstoneVariant.toString()));
            }

            default -> { return null; }
        }
    }

    private static String handleCollectionSinceLast() {
        return collectionSinceLast > 0
                ? formatCollectionName(collection) + " collection since last: " + formatNumber(collectionSinceLast)
                : formatCollectionName(collection) + " collection since last: Calculating...";
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

    private static String formatBazaarItemName(String name) {
        String[] words = name.split("_");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            if (i == 0) {
                formatted.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            } else {
                formatted.append(" ").append(word);
            }
        }
        return formatted.toString();
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

    private static String formatNumber(float number) {
        number = (float) Math.floor(number);

        if (number < 1_000) {
            return String.valueOf((int) number);
        } else if (number < 1_000_000) {
            return String.format("%.2fk", number / 1_000.0);
        } else if (number < 1_000_000_000) {
            return String.format("%.2fM", number / 1_000_000.0);
        } else {
            return String.format("%.2fB", number / 1_000_000_000.0);
        }
    }

    private static String formatNumberOrPlaceholder(float value) {
        return value > 0 ? formatNumber(value) : "Calculating...";
    }
}