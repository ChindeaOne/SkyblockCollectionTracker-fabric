package io.github.chindeaone.collectiontracker.utils.rendering;

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.GemstonesManager;
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices;
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices;
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices;
import io.github.chindeaone.collectiontracker.commands.CollectionTracker;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.ConfigHelper;

import io.github.chindeaone.collectiontracker.config.categories.Bazaar;
import io.github.chindeaone.collectiontracker.config.categories.Bazaar.BazaarType;
import io.github.chindeaone.collectiontracker.config.categories.overlay.CollectionOverlay;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates;
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import io.github.chindeaone.collectiontracker.utils.StringUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import java.util.List;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection;
import static io.github.chindeaone.collectiontracker.config.categories.overlay.MultiCollectionOverlay.TrackingOptions.COLLECTION;
import static io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates.*;
import static io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatNumber;

public class TextUtils {

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
                case COLLECTION_SINCE_LAST_TIMER -> addIfNotNull(list, handleCollectionSinceLastTimer());
            }
        }
    }

    private static void addIfNotNull(List<String> list, String line) {
        if (line != null) list.add(line);
    }

    private static String handleCollection() {
        if (CollectionsManager.collectionSource.equals("sacks")) return null;
        return collectionAmount >= 0
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

        boolean hasNpcPrice = NpcPrices.getNpcPrice(collection) != 0;

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
            default -> {
                return null;
            }
        }
    }

    private static String handleMoneyMade() {
        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableBazaar();
            ChatUtils.INSTANCE.sendMessage("§cYou cannot use Bazaar prices for this collection!", true);
            return null;
        }

        if (collectionType == null) return null; // no collection type (probably rift collection)


        boolean hasNpcPrice = NpcPrices.getNpcPrice(collection) != 0;

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
                return "$ made (Bazaar): " + formatNumberOrPlaceholder(localMoneyMade);
            }
            default -> {
                return null;
            }
        }
    }

    private static String handleCollectionSinceLast() {
        return collectionSinceLast > 0
                ? formatCollectionName(collection) + " collection since last: " + formatNumber(collectionSinceLast)
                : formatCollectionName(collection) + " collection since last: Calculating...";
    }

    private static String handleCollectionSinceLastTimer() {
        long totalSeconds = (System.currentTimeMillis() - lastCollectionTime) / 1000;
        if (totalSeconds < 60) {
            return "Last updated§f: " + totalSeconds + "s ago";
        }

        long min = totalSeconds / 60;
        long sec = totalSeconds % 60;

        return String.format("Last updated: %dm %ds ago", min, sec);
    }

    // Only if it has bazaar data and is enabled
    public static void updateTrackingExtraLines(List<String> list) {
        list.clear();
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
                case BAZAAR_PRICE_TYPE -> addIfNotNull(list, handleBazaarPriceType());
                case BAZAAR_ITEM -> addIfNotNull(list, handleBazaarItem());
                case BAZAAR_PRICE -> addIfNotNull(list, handleBazaarPrice());
            }
        }
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
                    } else
                        return "Bazaar item: " + StringUtils.formatBazaarItemName(BazaarCollectionsManager.superEnchantedRecipe.keySet().iterator().next());
                }
            }
            case "gemstone" -> {
                return "Bazaar variant: " + ConfigAccess.getGemstoneVariant();
            }
            default -> {
                return null;
            }
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
                    return "Item price: " + formatNumber((long) price);
                } else {
                    if (BazaarCollectionsManager.superEnchantedRecipe.isEmpty()) {
                        ConfigHelper.setBazaarType(BazaarType.ENCHANTED_VERSION);
                        return null;
                    } else {
                        float price = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? BazaarPrices.superEnchantedInstantBuy : BazaarPrices.superEnchantedInstantSell;
                        if (price == 0) {
                            return "Item price: Unknown price";
                        }
                        return "Item price: " + formatNumber((long) price);
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
            default -> {
                return null;
            }
        }
    }

    public static void updateMultiTrackingLines(List<String> list, List<String> expanded, boolean showPrefixes) {
        list.clear();
        for (String coll : CollectionTracker.collectionList) {
            if ("gemstone".equals(coll)) {
                boolean mainExpanded = expanded.contains("gemstone");
                boolean showingCollection = ConfigAccess.getTrackingOptions() == COLLECTION;
                String prefix = (showPrefixes && !showingCollection) ? (mainExpanded ? "§e[-]§r " : "§e[+]§r ") : "";

                if (mainExpanded) {
                    list.add(prefix + "§dGemstones:§r");

                    GemstonePrices.multiGemstoneRecipes.forEach((type, variants) -> {
                        if (MultiTrackingRates.INSTANCE.getSeenGemstones().contains(type)) {
                            String line = null;
                            switch (ConfigAccess.getTrackingOptions()) {
                                case COLLECTION_RATE -> line = handleCollectionPerHourMulti(type);
                                case COLLECTION_MADE -> line = handleCollectionSessionMulti(type);
                                case MONEY_RATE -> line = handleMoneyPerHourMulti(type);
                                case MONEY_MADE -> line = handleMoneyMadeMulti(type);
                            }
                            if (line != null) {
                                list.add("  " + line);
                            }
                        }
                    });
                } else {
                    String line = null;
                    switch (ConfigAccess.getTrackingOptions()) {
                        case COLLECTION -> line = handleCollectionMulti("gemstone");
                        case COLLECTION_RATE -> line = handleCollectionPerHourMulti("gemstone");
                        case COLLECTION_MADE -> line = handleCollectionSessionMulti("gemstone");
                        case MONEY_RATE -> line = handleMoneyPerHourMulti("gemstone");
                        case MONEY_MADE -> line = handleMoneyMadeMulti("gemstone");
                    }
                    if (line != null) {
                        list.add(prefix + "§d" + line + "§r");
                    } else {
                        list.add(prefix + "§dGemstones:§r");
                    }
                }
                continue;
            }

            String line = null;
            switch (ConfigAccess.getTrackingOptions()) {
                case COLLECTION -> line = handleCollectionMulti(coll);
                case COLLECTION_RATE -> line = handleCollectionPerHourMulti(coll);
                case COLLECTION_MADE -> line = handleCollectionSessionMulti(coll);
                case MONEY_RATE -> line = handleMoneyPerHourMulti(coll);
                case MONEY_MADE -> line = handleMoneyMadeMulti(coll);
            }
            if (line != null) {
                list.add(line);
            }
        }

        String suffix = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? "_INSTANT_BUY" : "_INSTANT_SELL";
        String type = ConfigAccess.getBazaarType() == BazaarType.ENCHANTED_VERSION ? "Enchanted version" : "Super Enchanted version";
        String variant = ConfigAccess.getGemstoneVariant().toString();

        switch (ConfigAccess.getTrackingOptions()) {
            case MONEY_RATE -> {
                if (!ConfigAccess.isUsingBazaar()) {
                    long total = MultiTrackingRates.INSTANCE.getMoneyPerHourNPC().entrySet().stream()
                            .filter(entry -> !entry.getKey().contains("_") || entry.getKey().endsWith("_" + variant))
                            .mapToLong(java.util.Map.Entry::getValue).filter(v -> v > 0).sum();
                    list.add("§eOverall $/h (NPC): " + formatNumber(total));
                } else {
                    long total = MultiTrackingRates.INSTANCE.getMoneyPerHourBazaar().entrySet().stream()
                            .filter(entry -> entry.getKey().endsWith(suffix))
                            .filter(entry -> entry.getKey().contains("_normal") || entry.getKey().contains("_" + type) || entry.getKey().contains("_" + variant))
                            .mapToLong(java.util.Map.Entry::getValue).filter(v -> v > 0).sum();
                    list.add("§eOverall $/h (Bazaar): " + formatNumber(total));
                }
            }
            case MONEY_MADE -> {
                if (!ConfigAccess.isUsingBazaar()) {
                    long total = MultiTrackingRates.INSTANCE.getMoneyMadeNPC().entrySet().stream()
                            .filter(entry -> !entry.getKey().contains("_") || entry.getKey().endsWith("_" + variant))
                            .mapToLong(java.util.Map.Entry::getValue).filter(v -> v > 0).sum();
                    list.add("§eOverall $ made (NPC): " + formatNumber(total));
                } else {
                    long total = MultiTrackingRates.INSTANCE.getMoneyMadeBazaar().entrySet().stream()
                            .filter(entry -> entry.getKey().endsWith(suffix))
                            .filter(entry -> entry.getKey().contains("_normal") || entry.getKey().contains("_" + type) || entry.getKey().contains("_" + variant))
                            .mapToLong(java.util.Map.Entry::getValue).filter(v -> v > 0).sum();
                    list.add("§eOverall $ made (Bazaar): " + formatNumber(total));
                }
            }
        }
    }

    private static String handleCollectionMulti(String coll) {
        return MultiTrackingRates.INSTANCE.getCollectionAmounts().getOrDefault(coll, -1L) >= 0
                ? formatCollectionName(coll) + " collection: " + formatNumber(MultiTrackingRates.INSTANCE.getCollectionAmounts().getOrDefault(coll, 0L))
                : formatCollectionName(coll) + " collection: Calculating...";
    }

    private static String handleCollectionSessionMulti(String coll) {
        return MultiTrackingRates.INSTANCE.getCollectionMade().getOrDefault(coll, -1L) > 0
                ? formatCollectionName(coll) + " collection (session): " + formatNumber(MultiTrackingRates.INSTANCE.getCollectionMade().getOrDefault(coll, 0L))
                : formatCollectionName(coll) + " collection (session): Calculating...";
    }

    private static String handleCollectionPerHourMulti(String coll) {
        return MultiTrackingRates.INSTANCE.getCollectionPerHour().getOrDefault(coll, -1L) > 0
                ? formatCollectionName(coll) + " Coll/h: " + formatNumber(MultiTrackingRates.INSTANCE.getCollectionPerHour().getOrDefault(coll, 0L))
                : formatCollectionName(coll) + " Coll/h: Calculating...";
    }

    private static String handleMoneyPerHourMulti(String coll) {
        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableBazaar();
            ChatUtils.INSTANCE.sendMessage("§cYou cannot use Bazaar prices for this collection!", true);
            return null;
        }

        boolean useBazaar = ConfigAccess.isUsingBazaar();
        if ("gemstone".equals(coll)) {
            long totalRate = 0;
            String variant = ConfigAccess.getGemstoneVariant().toString();
            String suffix = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? "_INSTANT_BUY" : "_INSTANT_SELL";

            for (String gem : MultiTrackingRates.INSTANCE.getSeenGemstones()) {
                if (useBazaar) {
                    totalRate += MultiTrackingRates.INSTANCE.getMoneyPerHourBazaar().getOrDefault((gem + "_" + variant).toUpperCase() + suffix, 0L);
                } else {
                    totalRate += MultiTrackingRates.INSTANCE.getMoneyPerHourNPC().getOrDefault((gem + "_" + variant).toUpperCase(), 0L);
                }
            }
            return "Gemstone $/h (" + (useBazaar ? "Bazaar" : "NPC") + "): " + formatNumberOrPlaceholder(totalRate);
        }

        if (!useBazaar) {
            String key = coll;
            if (MultiTrackingRates.INSTANCE.getSeenGemstones().contains(coll)) {
                String variant = ConfigAccess.getGemstoneVariant().toString();
                key = (coll + "_" + variant).toUpperCase();
            }

            long npcRate = MultiTrackingRates.INSTANCE.getMoneyPerHourNPC().getOrDefault(key, -1L);
            if (CollectionsManager.isRiftCollection(coll)) {
                return formatCollectionName(coll) + " Motes/h: " + formatNumberOrPlaceholder(npcRate);
            }
            return formatCollectionName(coll) + " $/h (NPC): " + formatNumberOrPlaceholder(npcRate);
        } else {
            String actualColl = coll;
            String gemstoneVariant = null;

            if (MultiTrackingRates.INSTANCE.getSeenGemstones().contains(coll)) {
                actualColl = "gemstone";
                String variant = ConfigAccess.getGemstoneVariant().toString();
                gemstoneVariant = (coll + "_" + variant).toUpperCase();
            }
            String suffix = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? "_INSTANT_BUY" : "_INSTANT_SELL";

            if (gemstoneVariant != null) {
                long rate = MultiTrackingRates.INSTANCE.getMoneyPerHourBazaar().getOrDefault(gemstoneVariant + suffix, 0L);
                return formatCollectionName(coll) + " $/h (Bazaar): " + formatNumberOrPlaceholder(rate);
            }

            String type = CollectionsManager.multiCollectionTypes.get(actualColl);

            if (type != null) {
                long rate = 0;
                switch (type) {
                    case "normal" -> rate = MultiTrackingRates.INSTANCE.getMoneyPerHourBazaar().getOrDefault(actualColl + "_normal" + suffix, 0L);
                    case "enchanted" -> {
                        String key = ConfigAccess.getBazaarType().equals(BazaarType.ENCHANTED_VERSION) ? "Enchanted version" : "Super Enchanted version";
                        rate = MultiTrackingRates.INSTANCE.getMoneyPerHourBazaar().getOrDefault(actualColl + "_" + key + suffix, 0L);
                    }
                }

                return formatCollectionName(coll) + " $/h (Bazaar): " + formatNumberOrPlaceholder(rate);
            } else {
                return formatCollectionName(coll) + " $/h (Bazaar): Calculating...";
            }
        }
    }

    private static String handleMoneyMadeMulti(String coll) {
        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableBazaar();
            ChatUtils.INSTANCE.sendMessage("§cYou cannot use Bazaar prices for this collection!", true);
            return null;
        }

        boolean useBazaar = ConfigAccess.isUsingBazaar();
        if ("gemstone".equals(coll)) {
            long totalMoney = 0;
            String variant = ConfigAccess.getGemstoneVariant().toString();
            String suffix = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? "_INSTANT_BUY" : "_INSTANT_SELL";

            for (String gem : MultiTrackingRates.INSTANCE.getSeenGemstones()) {
                if (useBazaar) {
                    totalMoney += MultiTrackingRates.INSTANCE.getMoneyMadeBazaar().getOrDefault((gem + "_" + variant).toUpperCase() + suffix, 0L);
                } else {
                    totalMoney += MultiTrackingRates.INSTANCE.getMoneyMadeNPC().getOrDefault((gem + "_" + variant).toUpperCase(), 0L);
                }
            }
            return "Gemstone $ made (" + (useBazaar ? "Bazaar" : "NPC") + "): " + formatNumberOrPlaceholder(totalMoney);
        }

        if (!useBazaar) {
            String key = coll;
            if (MultiTrackingRates.INSTANCE.getSeenGemstones().contains(coll)) {
                String variant = ConfigAccess.getGemstoneVariant().toString();
                key = (coll + "_" + variant).toUpperCase();
            }

            long npcMoney = MultiTrackingRates.INSTANCE.getMoneyMadeNPC().getOrDefault(key, -1L);
            if (CollectionsManager.isRiftCollection(coll)) {
                return formatCollectionName(coll) + " Motes made: " + formatNumberOrPlaceholder(npcMoney);
            }
            return formatCollectionName(coll) + " $ made (NPC): " + formatNumberOrPlaceholder(npcMoney);
        } else {
            String actualColl = coll;
            String gemstoneVariant = null;

            if (MultiTrackingRates.INSTANCE.getSeenGemstones().contains(coll)) {
                actualColl = "gemstone";
                String variant = ConfigAccess.getGemstoneVariant().toString();
                gemstoneVariant = (coll + "_" + variant).toUpperCase();
            }

            String suffix = ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? "_INSTANT_BUY" : "_INSTANT_SELL";

            if (gemstoneVariant != null) {
                long money = MultiTrackingRates.INSTANCE.getMoneyMadeBazaar().getOrDefault(gemstoneVariant + suffix, 0L);
                return formatCollectionName(coll) + " $ made (Bazaar): " + formatNumberOrPlaceholder(money);
            }

            String type = CollectionsManager.multiCollectionTypes.get(actualColl);

            if (type != null) {
                long money = 0;
                switch (type) {
                    case "normal" -> money = MultiTrackingRates.INSTANCE.getMoneyMadeBazaar().getOrDefault(actualColl + "_normal" + suffix, 0L);
                    case "enchanted" -> {
                        String key = ConfigAccess.getBazaarType().equals(BazaarType.ENCHANTED_VERSION) ? "Enchanted version" : "Super Enchanted version";
                        money = MultiTrackingRates.INSTANCE.getMoneyMadeBazaar().getOrDefault(actualColl + "_" + key + suffix, 0L);
                    }
                }

                return formatCollectionName(coll) + " $ made (Bazaar): " + formatNumberOrPlaceholder(money);
            } else {
                return formatCollectionName(coll) + " $ made (Bazaar): Calculating...";
            }
        }
    }

    public static void addToggleableSettingsLines(List<String> list) {
        list.add("");
        boolean isUsingBazaar = ConfigAccess.isUsingBazaar();
        if (isUsingBazaar) {
            list.add("§a[Bazaar Prices]");
            if (CollectionTracker.collectionList.contains("gemstone") || GemstonesManager.checkIfGemstone(collection)) {
                list.add("§e[" + ConfigAccess.getGemstoneVariant() + "]");
            }
            if ("enchanted".equals(collectionType) || CollectionsManager.multiCollectionTypes.containsValue("enchanted")) {
                if (ConfigAccess.getBazaarType().equals(BazaarType.ENCHANTED_VERSION)) {
                    list.add("§e[Enchanted version]");
                } else {
                    list.add("§e[Super Enchanted version]");
                }
            }
            list.add("§e[" + (ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ? "Instant Buy]" : "Instant Sell]"));
            list.add("§e[NPC Prices]");
            if (TrackingHandler.isTracking) {
                if (ConfigAccess.isShowExtraStats()) list.add("§a[Extra Stats]");
                else list.add("§e[Extra Stats]");
            }
        } else {
            list.add("§e[Bazaar Prices]");
            list.add("§a[NPC Prices]");
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
        String[] words = collection.split("[\\s_]+");
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

    public static String formatTime(double time) {
        int precision = ConfigAccess.getAbilityPrecision();
        String formatString = "%." + precision + "fs";
        return String.format(formatString, time);
    }
}
