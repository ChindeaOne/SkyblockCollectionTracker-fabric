package io.github.chindeaone.collectiontracker.util.rendering;

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices;
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices;
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.ConfigHelper;

import io.github.chindeaone.collectiontracker.config.categories.Bazaar;
import io.github.chindeaone.collectiontracker.config.categories.Bazaar.BazaarType;
import io.github.chindeaone.collectiontracker.config.categories.overlay.CollectionOverlay;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import io.github.chindeaone.collectiontracker.util.StringUtils;
import io.github.chindeaone.collectiontracker.util.chat.ChatListener;
import java.util.List;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.*;
import static io.github.chindeaone.collectiontracker.util.NumbersUtils.formatNumber;

public class TextUtils {

    private static boolean hasNpcPrice;

    public static void updateTrackingLines(List<String> list) {
        list.clear();
        if (ConfigAccess.getStatsText().isEmpty()) return;

        for (CollectionOverlay.OverlayText id : ConfigAccess.getStatsText()) {
            switch (id) {
                case COLLECTION -> addIfNotNull(list, handleCollection());
                case COLLECTION_SESSION -> addIfNotNull(list, handleCollectionSession());
                case COLL_PER_HOUR -> addIfNotNull(list, handleCollectionPerHour());
                case MONEY_PER_HOUR -> addIfNotNull(list, handleMoneyPerHour());
                case MONEY_MADE -> addIfNotNull(list, handleMoneyMade());
                case COLLECTION_SINCE_LAST -> addIfNotNull(list, handleCollectionSinceLast());
            }
        }
    }

    private static void addIfNotNull(List<String> list, String line) {
        if (line != null) list.add(line);
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

        if (collectionType == null) return null; // no collection type (probably rift collection)

        hasNpcPrice = NpcPrices.getNpcPrice(collection) != 0;

        if (!ConfigAccess.isUsingBazaar() && hasNpcPrice) {
            if (CollectionsManager.isRiftCollection(collection)) {
                // Use motes instead of money for rift collections
                return "Motes/h: " + formatNumberOrPlaceholder(moneyPerHourNPC);
            }
            return "$/h (NPC): " + formatNumberOrPlaceholder(moneyPerHourNPC);
        }

        if (!ConfigAccess.isUsingBazaar()) return null;

        long localMoneyPerHour;
        String suffix = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? "_INSTANT_BUY" : "_INSTANT_SELL";
        switch (collectionType) {
            case "normal" -> {
                localMoneyPerHour = moneyPerHourBazaar.getOrDefault(collectionType + suffix, 0L);
                return "$/h (Bazaar): " + formatNumberOrPlaceholder(localMoneyPerHour);
            }
            case "enchanted" -> {
                if (ConfigAccess.getBazaarType().equals(BazaarType.ENCHANTED_VERSION)) {
                    localMoneyPerHour = moneyPerHourBazaar.getOrDefault("Enchanted version" + suffix, 0L);
                    return "$/h (Bazaar): " + formatNumberOrPlaceholder(localMoneyPerHour);
                } else {
                    localMoneyPerHour = moneyPerHourBazaar.getOrDefault("Super Enchanted version" + suffix, -1L);
                    if (localMoneyPerHour == -1L) {
                        ConfigHelper.setBazaarType(BazaarType.ENCHANTED_VERSION);
                        return null;
                    } else return "$/h (Bazaar): " + formatNumberOrPlaceholder(localMoneyPerHour);
                }
            }
            case "gemstone" -> {
                localMoneyPerHour = moneyPerHourBazaar.getOrDefault(ConfigAccess.getGemstoneVariant() + suffix, 0L);
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

        if (collectionType == null) return null; // no collection type (probably rift collection)


        hasNpcPrice = NpcPrices.getNpcPrice(collection) != 0;

        if (!ConfigAccess.isUsingBazaar() && hasNpcPrice) {
            long moneyMadeNPC = moneyMade.get("NPC");
            if (CollectionsManager.isRiftCollection(collection)) {
                // Use motes instead of money for rift collections
                return "Motes made: " + formatNumberOrPlaceholder(moneyMadeNPC);
            }
            return "$ made (NPC): " + formatNumberOrPlaceholder(moneyMadeNPC);
        }

        if (!ConfigAccess.isUsingBazaar()) return null;

        long localMoneyMade;
        String suffix = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? "_INSTANT_BUY" : "_INSTANT_SELL";
        switch (collectionType) {
            case "normal" -> {
                localMoneyMade = moneyMade.getOrDefault(collectionType + suffix, 0L);
                return "$ made (Bazaar): " + formatNumberOrPlaceholder(localMoneyMade);
            }
            case "enchanted" -> {
                if (ConfigAccess.getBazaarType().equals(BazaarType.ENCHANTED_VERSION)) {
                    localMoneyMade = moneyMade.getOrDefault("Enchanted version" + suffix, 0L);
                    return "$ made (Bazaar): " + formatNumberOrPlaceholder(localMoneyMade);
                } else {
                    localMoneyMade = moneyMade.getOrDefault("Super Enchanted version" + suffix, -1L);
                    if (localMoneyMade == -1L) {
                        ConfigHelper.setBazaarType(BazaarType.ENCHANTED_VERSION);
                        return null;
                    } else return "$ made (Bazaar): " + formatNumberOrPlaceholder(localMoneyMade);
                }
            }
            case "gemstone" -> {
                localMoneyMade = moneyMade.getOrDefault(ConfigAccess.getGemstoneVariant() + suffix, 0L);
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

    // Only if it has bazaar data and is enabled
    public static void updateTrackingExtraLines(List<String> list) {
        if (!ConfigAccess.isShowExtraStats()) {
            list.clear();
            return;
        }

        if (!BazaarCollectionsManager.hasBazaarData) {
            ConfigHelper.disableExtraStats();
            ChatUtils.INSTANCE.sendMessage("§cNo Bazaar data available for extra stats!", true);
            list.clear();
            return;
        }

        if (collectionType.equals("normal") && ConfigAccess.isShowExtraStats()) {
            ConfigHelper.disableExtraStats();
            ChatUtils.INSTANCE.sendMessage("§cExtra stats are redundant here!", true);
            list.clear();
            return;
        }

        if (ConfigAccess.isShowExtraStats() && !ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableExtraStats();
            ChatUtils.INSTANCE.sendMessage("§cDisabled extra stats since you don't use Bazaar prices!", true);
            list.clear();
            return;
        }

        list.clear();

        list.add("§6§lExtra Stats:");
        for (CollectionOverlay.OverlayExtraText id : ConfigAccess.getExtraStatsText()) {
            switch (id) {
                case BAZAAR_PRICE_TYPE -> addIfNotNullExtra(list, handleBazaarPriceType());
                case BAZAAR_ITEM -> addIfNotNullExtra(list, handleBazaarItem());
                case BAZAAR_PRICE -> addIfNotNullExtra(list, handleBazaarPrice());
            }
        }
    }

    private static void addIfNotNullExtra(List<String> list, String line) {
        if (line != null) list.add(line);
    }

    private static String handleBazaarPriceType() {
        if (ConfigAccess.getBazaarPriceType().equals(Bazaar.BazaarPriceType.INSTANT_BUY)) {
            return "Price type: Instant Buy";
        } else {
            return "Price type: Instant Sell";
        }
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
                    float price = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? BazaarPrices.enchantedInstantBuy : BazaarPrices.enchantedInstantSell;
                    if (price == 0) {
                        return "Item price: Unknown price";
                    }
                    return  "Item price: " + formatNumber((long) price);
                } else {
                    if (BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                        ConfigHelper.setBazaarType(BazaarType.ENCHANTED_VERSION);
                        return null;
                    } else {
                        float price = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? BazaarPrices.superEnchantedInstantBuy : BazaarPrices.superEnchantedInstantSell;
                        if (price == 0) {
                            return "Item price: Unknown price";
                        }
                        return  "Item price: " + formatNumber((long) price);
                    }
                }
            }
            case "gemstone" -> {
                float price = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? GemstonePrices.getInstantBuyPrice(ConfigAccess.getGemstoneVariant().toString()) : GemstonePrices.getInstantSellPrice(ConfigAccess.getGemstoneVariant().toString());
                if (price == 0) {
                    return "Variant price: Unknown price";
                }
                return "Variant price: " + formatNumber((long) price);
            }
            default -> { return null; }
        }
    }

    public static String updateTimer() {
        long timeLeft = (ChatListener.getNextBuffTime() - System.currentTimeMillis()) / 1000;
        if (timeLeft <= 5) {
            return "§aTime left: §cSoon";
        }

        long minutes = timeLeft / 60;
        long seconds = timeLeft % 60;
        return String.format("§aTime left: %02d:%02d", minutes, seconds);
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

    public static String formatNumberOrPlaceholder(long value) {
        return value > 0 ? formatNumber(value) : "Calculating...";
    }
}