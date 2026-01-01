package io.github.chindeaone.collectiontracker.util.rendering;

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.GemstonesManager;
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices;
import io.github.chindeaone.collectiontracker.config.ModConfig;
import io.github.chindeaone.collectiontracker.config.categories.bazaar.BazaarConfig;
import io.github.chindeaone.collectiontracker.config.categories.overlay.OverlaySingle;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.commands.StartTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.*;
import static io.github.chindeaone.collectiontracker.tracker.TrackingRates.*;

public class TextUtils {

    static List<String> overlayLines = new ArrayList<>();

    public static void updateStats() {
        ModConfig config = Objects.requireNonNull(SkyblockCollectionTracker.configManager.getConfig());
        OverlaySingle overlay = config.getOverlay().overlaySingle;

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        if (!isTracking) {
            overlayLines.clear();
            return;
        }

        overlayLines.clear();
        for (OverlaySingle.OverlayExampleText id : overlay.statsText) {
            switch (id) {
                case GOLD_COLLECTION:
                    if (CollectionsManager.collectionSource.equals("collection") && collection != null) {
                        String collectionText = collectionAmount >= 0
                                ? formatCollectionName(collection) + " collection: " + formatNumber(collectionAmount)
                                : formatCollectionName(collection) + " collection: Calculating...";
                        overlayLines.add(collectionText);
                    }
                    break;
                case GOLD_COLLECTION_SESSION:
                    if (collection != null) {
                        String sessionText = collectionMade > 0
                                ? formatCollectionName(collection) + " collection (session): " + formatNumber(collectionMade)
                                : formatCollectionName(collection) + " collection (session): Calculating...";
                        overlayLines.add(sessionText);
                    }
                    break;
                case COLL_PER_HOUR:
                    String perHourText = collectionPerHour > 0
                            ? "Coll/h: " + formatNumber(collectionPerHour)
                            : "Coll/h: Calculating...";
                    overlayLines.add(perHourText);
                    break;
                case MONEY_PER_HOUR:
                    boolean hasNpcPrice = NpcPrices.getNpcPrice(collection) != -1;
                    if (!CollectionsManager.isRiftCollection(collection) && BazaarCollectionsManager.hasBazaarData) {
                        if (config.getBazaar().bazaarConfig.useBazaar) {
                            switch (collectionType) {
                                case "normal":
                                    float localMoneyPerHour = moneyPerHourBazaar.get(collectionType);
                                    if (localMoneyPerHour > 0) {
                                        overlayLines.add("$/h (Bazaar): " + formatNumber(localMoneyPerHour));
                                    } else {
                                        overlayLines.add("$/h (Bazaar): Calculating...");
                                    }
                                    break;
                                case "enchanted":
                                    if (config.getBazaar().bazaarConfig.bazaarType.equals(BazaarConfig.BazaarType.ENCHANTED_VERSION)) {
                                        float enchantedPrice = moneyPerHourBazaar.get("Enchanted version");
                                        if (enchantedPrice > 0) {
                                            overlayLines.add("$/h (Bazaar): " + formatNumber(enchantedPrice));
                                        } else {
                                            overlayLines.add("$/h (Bazaar): Calculating...");
                                        }
                                    } else {
                                        float superEnchantedPrice = moneyPerHourBazaar.get("Super Enchanted version");
                                        if (superEnchantedPrice == -1.0f) {
                                            config.getBazaar().bazaarConfig.bazaarType = BazaarConfig.BazaarType.ENCHANTED_VERSION;
                                        } else if (superEnchantedPrice > 0) {
                                            overlayLines.add("$/h (Bazaar): " + formatNumber(superEnchantedPrice));
                                        } else {
                                            overlayLines.add("$/h (Bazaar): Calculating...");
                                        }
                                    }
                                    break;
                                case "gemstone":
                                    float gemstonePrice = moneyPerHourBazaar.get(config.getBazaar().bazaarConfig.gemstoneVariant.toString());
                                    if (gemstonePrice > 0) {
                                        overlayLines.add("$/h (Bazaar): " + formatNumber(gemstonePrice));
                                    } else {
                                        overlayLines.add("$/h (Bazaar): Calculating...");
                                    }
                                    break;
                            }
                        } else if (hasNpcPrice) {
                            if (moneyPerHourNPC > 0) {
                                overlayLines.add("$/h (NPC): " + formatNumber(moneyPerHourNPC));
                            } else {
                                overlayLines.add("$/h (NPC): Calculating...");
                            }
                        }
                    } else if (config.getBazaar().bazaarConfig.useBazaar) {
                        config.getBazaar().bazaarConfig.useBazaar = false;
                        ChatUtils.INSTANCE.sendMessage("§cYou cannot use Bazaar prices for this collection!", true);
                    }
                    break;
                case EXTRAS:
                    if (config.getBazaar().bazaarConfig.useBazaar) {
                        if (GemstonesManager.checkIfGemstone(collection)) {
                            String extrasText = "Variant: " + config.getBazaar().bazaarConfig.gemstoneVariant.toString();
                            overlayLines.add(extrasText);
                        } else if (collectionType.equals("enchanted")) {
                            String itemName;
                            if (config.getBazaar().bazaarConfig.bazaarType.equals(BazaarConfig.BazaarType.ENCHANTED_VERSION)) {
                                itemName = formatBazaarItemName(BazaarCollectionsManager.enchantedRecipe.keySet().iterator().next());
                                overlayLines.add("Item: " + itemName);
                            } else {
                                itemName = formatBazaarItemName(BazaarCollectionsManager.superEnchantedRecipe.keySet().iterator().next());
                                overlayLines.add("Item: " + itemName);
                            }
                        }
                    }
                    break;
            }
        }
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
        return overlayLines;
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
                formattedName.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase());
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
}