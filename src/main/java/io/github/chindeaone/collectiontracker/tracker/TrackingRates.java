package io.github.chindeaone.collectiontracker.tracker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices;
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices;
import io.github.chindeaone.collectiontracker.collections.prices.NpcPrices;
import io.github.chindeaone.collectiontracker.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.commands.StartTracker.collection;
import static io.github.chindeaone.collectiontracker.gui.overlays.CollectionOverlay.overlayDirty;
import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.getUptimeInSeconds;

public class TrackingRates {

    public static final Logger logger = LogManager.getLogger(TrackingRates.class);

    // Collection tracking data
    public static volatile float collectionAmount;
    public static volatile float collectionPerHour;
    public static volatile float collectionMade;
    public static volatile float collectionSinceLast;
    public static long previousCollection = -1L;
    public static long sessionStartCollection = -1L;
    // Highest and lowest rates
    public static volatile float highestCollectionPerHour = 0;
    public static volatile float lowestCollectionPerHour = Float.MAX_VALUE;

    // AFK detection
    public static boolean afk = false;
    private static int unchangedStreak = 0;
    private static final int THRESHOLD = 2; // Number of checks before considering AFK

    // Money tracking data
    // NPC
    public static volatile float moneyPerHourNPC;
    // Highest and lowest rates
    public static volatile float highestRatePerHourNPC = 0;
    public static volatile float lowestRatePerHourNPC = Float.MAX_VALUE;

    // Bazaar
    public static Map<String, Float> moneyMade = new ConcurrentHashMap<>();
    public static Map<String, Float> moneyPerHourBazaar = new ConcurrentHashMap<>();
    // Highest and lowest rates
    public static Map<String, Float> lowestRatesPerHourBazaar = new ConcurrentHashMap<>();
    public static Map<String, Float> highestRatesPerHourBazaar = new ConcurrentHashMap<>();

    public static void calculateRates(String jsonResponse) {
        // Set bazaar config
        ModConfig config = SkyblockCollectionTracker.configManager.getConfig();
        assert config != null;

        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        long currentCollection = jsonObject.entrySet().iterator().next().getValue().getAsLong();

        // Set starting collection
        if (sessionStartCollection == -1L) {
            sessionStartCollection = currentCollection;

        }
        collectionSinceLast = previousCollection != -1L ? (float) Math.floor(currentCollection - previousCollection) : 0;

        // Set player as AFK, else update previousCollection
        if (currentCollection == previousCollection) {
            unchangedStreak++;
            if (unchangedStreak >= THRESHOLD) {
                afk = true;
                if (TrackingHandlerClass.isTracking) {
                    TrackingHandlerClass.stopTracking();
                }
                unchangedStreak = 0;
                return;
            }
        } else {
            previousCollection = currentCollection;
            unchangedStreak = 0;
        }

        // Add additional uptime for failed request
        if (unchangedStreak > 0) {
            return;
        }

        long uptime = getUptimeInSeconds();
        float collectedSinceStart = currentCollection - sessionStartCollection;

        float bazaarPrice;
        float enchantedPrice;
        float superEnchantedPrice;

        float priceNPC = NpcPrices.getNpcPrice(collection);
        moneyMade.put("NPC", uptime > 0 ? (float) Math.floor(priceNPC * collectedSinceStart) : 0);

        float computed;
        if (!CollectionsManager.isRiftCollection(collection) && BazaarCollectionsManager.hasBazaarData) {
            switch (collectionType) {
                case "normal" -> {
                    bazaarPrice = BazaarPrices.normalPrice;
                    computed = uptime > 0 ? (float) Math.floor(bazaarPrice * (collectedSinceStart / 160) / (uptime / 3600.0f)) : 0;
                    moneyPerHourBazaar.put(collectionType, computed);
                    updateBazaarExtremes(collectionType, computed);
                    moneyMade.put(collectionType, uptime > 0 ? (float) Math.floor(bazaarPrice * collectedSinceStart) : 0);
                }
                case "enchanted" -> {
                    enchantedPrice = BazaarPrices.enchantedPrice;
                    superEnchantedPrice = BazaarPrices.superEnchantedPrice;

                    computed = uptime > 0 ? (float) Math.floor(enchantedPrice * (collectedSinceStart / BazaarCollectionsManager.enchantedRecipe.values().iterator().next()) / (uptime / 3600.0f)) : 0;
                    moneyPerHourBazaar.put("Enchanted version", computed);
                    updateBazaarExtremes("Enchanted version", computed);
                    moneyMade.put("Enchanted version", uptime > 0 ? (float) Math.floor(enchantedPrice * (collectedSinceStart / BazaarCollectionsManager.enchantedRecipe.values().iterator().next())) : 0);

                    if (!(superEnchantedPrice == 0.0f)) {
                        computed = uptime > 0 ? (float) Math.floor(superEnchantedPrice * (collectedSinceStart / BazaarCollectionsManager.superEnchantedRecipe.values().iterator().next()) / (uptime / 3600.0f)) : 0;
                        moneyPerHourBazaar.put("Super Enchanted version", computed);
                        updateBazaarExtremes("Super Enchanted version", computed);
                        moneyMade.put("Super Enchanted version", uptime > 0 ? (float) Math.floor(superEnchantedPrice * (collectedSinceStart / BazaarCollectionsManager.superEnchantedRecipe.values().iterator().next())) : 0);
                    }
                }
                case "gemstone" -> {
                    for (String key : GemstonePrices.gemstonePrices.keySet()) {
                        computed = uptime > 0 ? (float) Math.floor(GemstonePrices.getPrice(key) * (collectedSinceStart / GemstonePrices.recipes.get(key)) / (uptime / 3600.0f)) : 0;
                        moneyPerHourBazaar.put(key, computed);
                        updateBazaarExtremes(key, computed);
                        moneyMade.put(key, uptime > 0 ? (float) Math.floor(GemstonePrices.getPrice(key) * (collectedSinceStart / GemstonePrices.recipes.get(key))) : 0);
                    }
                }
            }
        }

        // Update values
        collectionAmount = (float) Math.floor(currentCollection);
        collectionPerHour = uptime > 0 ? (float) Math.floor((collectedSinceStart / uptime) * 3600) : 0;
        collectionMade = (float) Math.floor(collectedSinceStart);
        moneyPerHourNPC = uptime > 0 ? (float) Math.floor(priceNPC * collectedSinceStart / (uptime / 3600.0f)) : 0;

        // Update highest and lowest rates
        if (collectionPerHour > highestCollectionPerHour) {
            highestCollectionPerHour = collectionPerHour;
        }
        if (collectionPerHour < lowestCollectionPerHour && collectionPerHour > 0) {
            lowestCollectionPerHour = collectionPerHour;
        }

        if (moneyPerHourNPC > highestRatePerHourNPC) {
            highestRatePerHourNPC = moneyPerHourNPC;
        }
        if (moneyPerHourNPC < lowestRatePerHourNPC && moneyPerHourNPC > 0) {
            lowestRatePerHourNPC = moneyPerHourNPC;
        }

        fillBazaarExtremesFromCurrent(); // Ensure extremes are initialized

        // Trigger overlay update
        overlayDirty = true;
    }

    private static void fillBazaarExtremesFromCurrent() {
        // Only initialize if both extremes maps are empty and there's data to copy
        if (!moneyPerHourBazaar.isEmpty() && lowestRatesPerHourBazaar.isEmpty() && highestRatesPerHourBazaar.isEmpty()) {
            for (Map.Entry<String, Float> e : moneyPerHourBazaar.entrySet()) {
                String key = e.getKey();
                float v = e.getValue() != null ? e.getValue() : 0f;
                if (v <= 0f) continue; // Skip non-positive values
                lowestRatesPerHourBazaar.putIfAbsent(key, v);
                highestRatesPerHourBazaar.putIfAbsent(key, v);
            }
        }
    }

    private static void updateBazaarExtremes(String key, float value) {
        if (value <= 0f || key == null) return;

        lowestRatesPerHourBazaar.compute(key, (k, old) -> (old == null) ? value : Math.min(old, value));

        highestRatesPerHourBazaar.compute(key, (k, old) -> (old == null) ? value : Math.max(old, value));
    }
}
