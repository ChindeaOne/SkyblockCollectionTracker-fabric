package io.github.chindeaone.collectiontracker.tracker.collection;

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices;
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices;
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices;
import io.github.chindeaone.collectiontracker.gui.overlays.CollectionOverlay;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.commands.CollectionTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler.getUptimeInSeconds;

public class TrackingRates {

    public static final Logger logger = LogManager.getLogger(TrackingRates.class);

    // Collection tracking data
    public static volatile long collectionAmount;
    public static volatile long collectionPerHour;
    public static volatile long collectionMade;
    public static volatile long collectionSinceLast;
    public static volatile long sessionStartCollection = -1L;

    // Sacks tracking data
    public static volatile long lastApiCollection = -1L;
    public static volatile long sacksCollectionGained = 0L;

    // Highest and lowest rates
    public static volatile long highestCollectionPerHour = 0;
    public static volatile long lowestCollectionPerHour = Long.MAX_VALUE;

    // AFK detection
    public static boolean afk = false;
    private static int unchangedStreak = 0;
    private static final int THRESHOLD = 2; // Number of checks before considering AFK

    // Money tracking data
    // NPC
    public static volatile long moneyPerHourNPC;
    // Highest and lowest rates
    public static volatile long highestRatePerHourNPC = 0;
    public static volatile long lowestRatePerHourNPC = Long.MAX_VALUE;

    // Bazaar
    public static Map<String, Long> moneyMade = new ConcurrentHashMap<>();
    public static Map<String, Long> moneyPerHourBazaar = new ConcurrentHashMap<>();
    // Highest and lowest rates
    public static Map<String, Long> lowestRatesPerHourBazaar = new ConcurrentHashMap<>();
    public static Map<String, Long> highestRatesPerHourBazaar = new ConcurrentHashMap<>();

    public static synchronized void calculateRates(long value, boolean isUsingSacks) {
        long currentCollection;

        // If using sacks, adjust currentCollection accordingly
        if (isUsingSacks) {
            if (lastApiCollection == -1L) {
                return; // wait for the next API call (should never happen but just in case)
            }
            // 'value' here is what you gained from sacks since last check
            sacksCollectionGained += value; // update sacks gained
            currentCollection = lastApiCollection + sacksCollectionGained; // increase current collection

            collectionSinceLast = value; // what you gained is exactly the 'value'

            logger.info("[SCT]: Current collection for '{}' (using sacks) is {}", collection, currentCollection);
            logger.info("[SCT]: Change in collection detected (using sacks). Old collection: '{}'. New collection: '{}'.", currentCollection - value, currentCollection);
        } else {
            // 'value' here is the actual collection value from API
            sacksCollectionGained = 0L; // reset sacks gained
            currentCollection = value; // set current collection to API value

            collectionSinceLast = lastApiCollection != -1L ? Math.max(0, currentCollection - lastApiCollection) : 0; // calculate since last from API

            logger.info("[SCT]: Current collection for '{}' is {}", collection, currentCollection);
            logger.info("[SCT]: Change in collection detected. Old collection: '{}'. New collection: '{}'.", lastApiCollection, currentCollection);

            // Set player as AFK, else update previousCollection
            if (currentCollection == lastApiCollection) {
                logger.info("[SCT]: No change in collection detected. Incrementing unchanged streak.");
                unchangedStreak++;
                if (unchangedStreak >= THRESHOLD) {
                    afk = true;
                    TrackingHandler.stopTracking();
                    unchangedStreak = 0;
                    return;
                }
            } else {
                lastApiCollection = currentCollection;
                unchangedStreak = 0;
            }
        }

        logger.info("[SCT]: Collection since last check is {}.", collectionSinceLast);

        // Set starting collection
        if (sessionStartCollection == -1L) {
            sessionStartCollection = currentCollection;
            unchangedStreak = 0;
        }

        // Add additional uptime for failed request
        if (unchangedStreak > 0) {
            return;
        }

        long uptime = getUptimeInSeconds();
        long collectedSinceStart = currentCollection - sessionStartCollection;

        float bazaarPrice;
        float enchantedPrice;
        float superEnchantedPrice;

        int priceNPC = NpcPrices.getNpcPrice(collection);
        moneyMade.put("NPC", uptime > 0 ? (long) Math.floor(priceNPC * (double) collectedSinceStart) : 0);

        long computed;
        if (BazaarCollectionsManager.hasBazaarData) {
            switch (collectionType) {
                case "normal" -> {
                    bazaarPrice = BazaarPrices.normalPrice;
                    computed = uptime > 0 ? (long) Math.floor(bazaarPrice * ((double) collectedSinceStart / 160) / (uptime / 3600.0)) : 0;
                    moneyPerHourBazaar.put(collectionType, computed);
                    updateBazaarExtremes(collectionType, computed);
                    moneyMade.put(collectionType, uptime > 0 ? (long) Math.floor(bazaarPrice * collectedSinceStart) : 0);
                }
                case "enchanted" -> {
                    enchantedPrice = BazaarPrices.enchantedPrice;
                    superEnchantedPrice = BazaarPrices.superEnchantedPrice;

                    computed = uptime > 0 ? (long) Math.floor(enchantedPrice * ((double) collectedSinceStart / BazaarCollectionsManager.enchantedRecipe.values().iterator().next()) / (uptime / 3600.0)) : 0;
                    moneyPerHourBazaar.put("Enchanted version", computed);
                    updateBazaarExtremes("Enchanted version", computed);
                    moneyMade.put("Enchanted version", uptime > 0 ? (long) Math.floor(enchantedPrice * ((double) collectedSinceStart / BazaarCollectionsManager.enchantedRecipe.values().iterator().next())) : 0);

                    if (!(superEnchantedPrice == 0.0f)) {
                        computed = uptime > 0 ? (long) Math.floor(superEnchantedPrice * ((double) collectedSinceStart / BazaarCollectionsManager.superEnchantedRecipe.values().iterator().next()) / (uptime / 3600.0)) : 0;
                        moneyPerHourBazaar.put("Super Enchanted version", computed);
                        updateBazaarExtremes("Super Enchanted version", computed);
                        moneyMade.put("Super Enchanted version", uptime > 0 ? (long) Math.floor(superEnchantedPrice * ((double) collectedSinceStart / BazaarCollectionsManager.superEnchantedRecipe.values().iterator().next())) : 0);
                    }
                }
                case "gemstone" -> {
                    for (String key : GemstonePrices.gemstonePrices.keySet()) {
                        computed = uptime > 0 ? (long) Math.floor(GemstonePrices.getPrice(key) * ((double) collectedSinceStart / GemstonePrices.recipes.get(key)) / (uptime / 3600.0)) : 0;
                        moneyPerHourBazaar.put(key, computed);
                        updateBazaarExtremes(key, computed);
                        moneyMade.put(key, uptime > 0 ? (long) Math.floor(GemstonePrices.getPrice(key) * ((double) collectedSinceStart / GemstonePrices.recipes.get(key))) : 0);
                    }
                }
            }
        }

        // Update values
        collectionAmount = currentCollection;
        collectionPerHour = uptime > 0 ? (long) Math.floor(collectedSinceStart / (uptime / 3600.0)) : 0;
        collectionMade = collectedSinceStart;
        moneyPerHourNPC = uptime > 0 ? (long) Math.floor(priceNPC * collectedSinceStart / (uptime / 3600.0)) : 0;

        // Update highest and lowest rates
        if (collectionPerHour > highestCollectionPerHour && collectionPerHour > 0) {
            highestCollectionPerHour = collectionPerHour;
        }
        if (collectionPerHour < lowestCollectionPerHour && collectionPerHour > 0) {
            lowestCollectionPerHour = collectionPerHour;
        }

        if (moneyPerHourNPC > highestRatePerHourNPC && moneyPerHourNPC > 0) {
            highestRatePerHourNPC = moneyPerHourNPC;
        }
        if (moneyPerHourNPC < lowestRatePerHourNPC && moneyPerHourNPC > 0) {
            lowestRatePerHourNPC = moneyPerHourNPC;
        }

        fillBazaarExtremesFromCurrent(); // Ensure extremes are initialized

        // Trigger tracking overlay update
        CollectionOverlay.trackingDirty = true;
    }

    private static void fillBazaarExtremesFromCurrent() {
        // Only initialize if both extremes maps are empty and there's data to copy
        if (!moneyPerHourBazaar.isEmpty() && lowestRatesPerHourBazaar.isEmpty() && highestRatesPerHourBazaar.isEmpty()) {
            for (Map.Entry<String, Long> e : moneyPerHourBazaar.entrySet()) {
                String key = e.getKey();
                Long val = e.getValue();
                // skip unwanted values
                if (val <= 0L || key == null) continue;
                long v = val;
                lowestRatesPerHourBazaar.putIfAbsent(key, v);
                highestRatesPerHourBazaar.putIfAbsent(key, v);
            }
        }
    }

    private static void updateBazaarExtremes(String key, long value) {
        if (key == null || value <= 0L) return;

        lowestRatesPerHourBazaar.compute(key, (k, old) -> (old == null) ? value : Math.min(old, value));

        highestRatesPerHourBazaar.compute(key, (k, old) -> (old == null) ? value : Math.max(old, value));
    }
}