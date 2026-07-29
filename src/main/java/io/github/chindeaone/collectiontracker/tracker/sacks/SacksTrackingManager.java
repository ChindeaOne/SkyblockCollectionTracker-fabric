package io.github.chindeaone.collectiontracker.tracker.sacks;

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.GemstonesManager;
import io.github.chindeaone.collectiontracker.commands.CollectionTracker;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates;
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class SacksTrackingManager {

    private static final Logger logger = LogManager.getLogger(SacksTrackingManager.class);

    public static void onSacksGain(Map<String, Integer> sacksDetails) {

        if (TrackingHandler.isTracking) {
            handleTracking(sacksDetails);
            return;
        }

        if (MultiTrackingHandler.isMultiTracking()) {
            handleMultiTracking(sacksDetails);
        }
    }

    private static void handleTracking(Map<String, Integer> sacksDetails) {
        String collectionName = CollectionTracker.collection;
        String type = CollectionsManager.collectionType;
        long totalAmount = 0;

        Map<String, Integer> normalizedEnchantedMap = normalizeMap(BazaarCollectionsManager.enchantedRecipe, false, collectionName);
        Map<String, Integer> normalizedSuperEnchantedMap = normalizeMap(BazaarCollectionsManager.superEnchantedRecipe, true, collectionName);

        for (Map.Entry<String, Integer> entry : sacksDetails.entrySet()) {
            String itemName = entry.getKey();
            int amount = entry.getValue();

            Integer enchantedMultiplier = normalizedEnchantedMap.get(itemName);
            Integer superEnchantedMultiplier = normalizedSuperEnchantedMap.get(itemName);

            boolean isEnchanted = enchantedMultiplier != null;
            boolean isSuperEnchanted = superEnchantedMultiplier != null;

            if (type == null) {
                logger.error("[SCT]: Collection type is null for collection: {}", collectionName);
                return;
            }

            boolean matchesCollection = type.equals("gemstone")
                    ? itemName.contains(collectionName)
                    : itemName.equals(collectionName);

            if (!matchesCollection && !isEnchanted && !isSuperEnchanted) continue;

            if (type.equals("gemstone")) {
                totalAmount += (long) amount * getGemstoneMultiplier(itemName);
            } else if (type.equals("enchanted")) {
                if (isSuperEnchanted) {
                    totalAmount += (long) amount * superEnchantedMultiplier;
                } else if (isEnchanted) {
                    totalAmount += (long) amount * enchantedMultiplier;
                } else {
                    totalAmount += amount;
                }
            } else {
                totalAmount += amount;
            }
        }

        if (totalAmount > 0) {
            TrackingRates.calculateRates(totalAmount);
        }
    }

    private static void handleMultiTracking(Map<String, Integer> sacksDetails) {
        Map<String, Long> gains = new HashMap<>();

        for (String coll : CollectionTracker.collectionList) {
            String type = CollectionsManager.multiCollectionTypes.get(coll);

            if (type == null) {
                // gemstones
                continue;
            }

            long totalAmount = 0;
            Map<String, Integer> normalizedEnchantedMap = normalizeMap(BazaarCollectionsManager.multiEnchantedRecipes.getOrDefault(coll, new HashMap<>()), false, coll);
            Map<String, Integer> normalizedSuperEnchantedMap = normalizeMap(BazaarCollectionsManager.multiSuperEnchantedRecipes.getOrDefault(coll, new HashMap<>()), true, coll);

            for (Map.Entry<String, Integer> entry : sacksDetails.entrySet()) {
                String itemName = entry.getKey();
                int amount = entry.getValue();

                if ("enchanted".equals(type)) {
                    Integer enchantedMultiplier = normalizedEnchantedMap.get(itemName);
                    Integer superEnchantedMultiplier = normalizedSuperEnchantedMap.get(itemName);

                    boolean isEnchanted = enchantedMultiplier != null;
                    boolean isSuperEnchanted = superEnchantedMultiplier != null;

                    if (!itemName.equals(coll) && !isEnchanted && !isSuperEnchanted) continue;

                    if (isSuperEnchanted) {
                        totalAmount += (long) amount * superEnchantedMultiplier;
                    } else if (isEnchanted) {
                        totalAmount += (long) amount * enchantedMultiplier;
                    } else {
                        totalAmount += amount;
                    }
                } else {
                    if (!itemName.equals(coll)) continue;

                    totalAmount += amount;
                }
            }

            if (totalAmount > 0) {
                gains.put(coll, totalAmount);
            }
        }

        if (CollectionTracker.collectionList.contains("gemstone")) {
            long generalGemstoneGains = 0;
            for (Map.Entry<String, Integer> entry : sacksDetails.entrySet()) {
                String itemName = entry.getKey();
                if (itemName.contains("gemstone")) {
                    long gain = (long) entry.getValue() * getGemstoneMultiplier(itemName);
                    generalGemstoneGains += gain;

                    String gemstoneType = null;
                    if (GemstonesManager.gemstones != null) {
                        for (String g : GemstonesManager.gemstones) {
                            if (itemName.contains(g.toLowerCase())) {
                                gemstoneType = g.toLowerCase();
                                break;
                            }
                        }
                    }
                    if (gemstoneType != null) {
                        gains.merge(gemstoneType, gain, Long::sum);
                    }
                }
            }
            if (generalGemstoneGains > 0) {
                gains.merge("gemstone", generalGemstoneGains, Long::sum);
            }
        }

        if (!gains.isEmpty()) {
            MultiTrackingRates.calculateMultiRates(gains);
        }
    }

    private static int getGemstoneMultiplier(String itemName) {
        return switch (itemName) {
            case String s when s.contains("flawless") -> 80 * 80 * 80;
            case String s when s.contains("fine") -> 80 * 80;
            case String s when s.contains("flawed") -> 80;
            default -> 1;
        };
    }

    private static Map<String, Integer> normalizeMap(Map<String, Integer> map, boolean isSuperEnchanted, String collectionName) {
        Map<String, Integer> normalizedMap = new HashMap<>();

        Map<String, String> overrides = isSuperEnchanted ? SUPER_ENCHANTED_DISPLAY_OVERRIDES : ENCHANTED_DISPLAY_OVERRIDES;

        String override = overrides.get(collectionName);

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey().toLowerCase().replace("_", " ");
            normalizedMap.put(override != null ? override : key, entry.getValue());
        }

        return normalizedMap;
    }

    private static final Map<String, String> ENCHANTED_DISPLAY_OVERRIDES = Map.ofEntries(
            Map.entry("gold ingot", "enchanted gold ingot"),
            Map.entry("iron ingot", "enchanted iron ingot"),
            Map.entry("redstone dust", "enchanted redstone dust"),
            Map.entry("end stone", "enchanted end stone"),
            Map.entry("nether quartz", "enchanted nether quartz"),
            Map.entry("cocoa beans", "enchanted cocoa beans"),
            Map.entry("nether wart", "enchanted nether wart"),
            Map.entry("melon slice", "enchanted melon slice"),
            Map.entry("raw rabbit", "enchanted raw rabbit"),
            Map.entry("raw mutton", "enchanted raw mutton"),
            Map.entry("raw porkchop", "enchanted raw porkchop"),
            Map.entry("slimeball", "enchanted slimeball"),
            Map.entry("lily pad", "enchanted lily pad"),
            Map.entry("ink sac", "enchanted ink sac"),
            Map.entry("raw cod", "enchanted raw cod"),
            Map.entry("tropical fish", "enchanted tropical fish"),
            Map.entry("magmafish", "gold magmafish"),
            Map.entry("lotus", "gold lotus")
    );

    private static final Map<String, String> SUPER_ENCHANTED_DISPLAY_OVERRIDES = Map.ofEntries(
            Map.entry("red mushroom", "enchanted red mushroom block"),
            Map.entry("brown mushroom", "enchanted brown mushroom block"),
            Map.entry("nether wart", "mutant nether wart"),
            Map.entry("melon slice", "enchanted melon"),
            Map.entry("raw porkchop", "enchanted cooked porkchop"),
            Map.entry("lily pad", "condensed lily pad"),
            Map.entry("raw cod", "enchanted cooked cod"),
            Map.entry("magmafish", "silver magmafish"),
            Map.entry("lotus", "silver lotus")
    );
}