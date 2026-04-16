package io.github.chindeaone.collectiontracker.tracker.sacks;

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.GemstonesManager;
import io.github.chindeaone.collectiontracker.commands.CollectionTracker;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates;
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates;

import java.util.HashMap;
import java.util.Map;

public class SacksTrackingManager {

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

        Map<String, Integer> normalizedEnchantedMap = normalizeMap(BazaarCollectionsManager.enchantedRecipe);
        Map<String, Integer> normalizedSuperEnchantedMap = normalizeMap(BazaarCollectionsManager.superEnchantedRecipe);

        for (Map.Entry<String, Integer> entry : sacksDetails.entrySet()) {
            String itemName = entry.getKey();
            int amount = entry.getValue();

            boolean isEnchanted = !normalizedEnchantedMap.isEmpty() && normalizedEnchantedMap.keySet().stream().anyMatch(itemName::contains);
            boolean isSuperEnchanted = !normalizedSuperEnchantedMap.isEmpty() && normalizedSuperEnchantedMap.keySet().stream().anyMatch(itemName::contains);

            if (!itemName.contains(collectionName)) continue;

            if (type.equals("gemstone")) {
                totalAmount += (long) amount * getGemstoneMultiplier(itemName);
            } else if (type.equals("enchanted")) {
                if (isSuperEnchanted) {
                    totalAmount += (long) amount * normalizedSuperEnchantedMap.values().iterator().next();
                } else if (isEnchanted) {
                    totalAmount += (long) amount * normalizedEnchantedMap.values().iterator().next();
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

            long totalAmount = 0;
            Map<String, Integer> normalizedEnchantedMap = normalizeMap(BazaarCollectionsManager.multiEnchantedRecipes.getOrDefault(coll, new HashMap<>()));
            Map<String, Integer> normalizedSuperEnchantedMap = normalizeMap(BazaarCollectionsManager.multiSuperEnchantedRecipes.getOrDefault(coll, new HashMap<>()));

            for (Map.Entry<String, Integer> entry : sacksDetails.entrySet()) {
                String itemName = entry.getKey();
                int amount = entry.getValue();

                if (!itemName.contains(coll)) continue;

                if ("gemstone".equals(type) || GemstonesManager.checkIfGemstone(coll)) {
                    long gain = (long) amount * getGemstoneMultiplier(itemName);
                    totalAmount += gain;
                } else if ("enchanted".equals(type)) {
                    boolean isEnchanted = !normalizedEnchantedMap.isEmpty() && normalizedEnchantedMap.keySet().stream().anyMatch(itemName::contains);
                    boolean isSuperEnchanted = !normalizedSuperEnchantedMap.isEmpty() && normalizedSuperEnchantedMap.keySet().stream().anyMatch(itemName::contains);

                    if (isSuperEnchanted) {
                        long gain = (long) amount * normalizedSuperEnchantedMap.values().iterator().next();
                        totalAmount += gain;
                    } else if (isEnchanted) {
                        long gain = (long) amount * normalizedEnchantedMap.values().iterator().next();
                        totalAmount += gain;
                    } else {
                        totalAmount += amount;
                    }
                } else {
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

    private static Map<String, Integer> normalizeMap(Map<String, Integer> map) {
        Map <String, Integer> normalizedMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey().toLowerCase().replace("_", " ");
            normalizedMap.put(key, entry.getValue());
        }
        return normalizedMap;
    }
}