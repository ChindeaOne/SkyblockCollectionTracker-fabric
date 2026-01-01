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

import java.util.HashMap;
import java.util.Map;

import static io.github.chindeaone.collectiontracker.collections.CollectionsManager.collectionType;
import static io.github.chindeaone.collectiontracker.commands.StartTracker.collection;
import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.getUptimeInSeconds;
import static io.github.chindeaone.collectiontracker.util.rendering.TextUtils.updateStats;

public class TrackingRates {

    public static final Logger logger = LogManager.getLogger(TrackingRates.class);

    public static float previousCollection = -1;
    public static float sessionStartCollection = -1;
    public static boolean afk = false;

    public static float collectionAmount;
    public static float collectionPerHour;
    public static float collectionMade;
    public static float moneyPerHourNPC;
    public static Map<String, Float> moneyPerHourBazaar = new HashMap<>();

    public static void calculateRates(String jsonResponse) {
        // Set bazaar config
        ModConfig config = SkyblockCollectionTracker.configManager.getConfig();
        assert config != null;

        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        float currentCollection = jsonObject.entrySet().iterator().next().getValue().getAsFloat();

        // Set starting collection
        if (sessionStartCollection == -1) {
            sessionStartCollection = currentCollection;

        }
        // Set player as AFK, else update previousCollection
        if (currentCollection == previousCollection) {
            afk = true;
            if (TrackingHandlerClass.isTracking) {
                TrackingHandlerClass.stopTracking();
            }
            return;
        } else {
            previousCollection = currentCollection;
        }

        long uptime = getUptimeInSeconds();
        float collectedSinceStart = currentCollection - sessionStartCollection;

        float bazaarPrice;
        float enchantedPrice;
        float superEnchantedPrice;

        float priceNPC = NpcPrices.getNpcPrice(collection);

        if(!CollectionsManager.isRiftCollection(collection) && BazaarCollectionsManager.hasBazaarData) {
            switch (collectionType) {
                case "normal":
                    bazaarPrice = BazaarPrices.normalPrice;
                    moneyPerHourBazaar.put(collectionType, uptime > 0 ? (float) Math.floor(bazaarPrice * (collectedSinceStart / 160) / (uptime / 3600.0f)) : 0);
                    break;
                case "enchanted":
                    enchantedPrice = BazaarPrices.enchantedPrice;
                    superEnchantedPrice = BazaarPrices.superEnchantedPrice;

                    moneyPerHourBazaar.put("Enchanted version", uptime > 0 ? (float) Math.floor(enchantedPrice * (collectedSinceStart / BazaarCollectionsManager.enchantedRecipe.values().iterator().next()) / (uptime / 3600.0f)) : 0);
                    if(superEnchantedPrice == 0.0f){
                        moneyPerHourBazaar.put("Super Enchanted version", -1.0f);
                    } else {
                        moneyPerHourBazaar.put("Super Enchanted version", uptime > 0 ? (float) Math.floor(superEnchantedPrice * (collectedSinceStart / BazaarCollectionsManager.superEnchantedRecipe.values().iterator().next()) / (uptime / 3600.0f)) : 0);
                    }
                    break;
                case "gemstone":
                    for (String key : GemstonePrices.gemstonePrices.keySet()) {
                        moneyPerHourBazaar.put(key, uptime > 0 ? (float) Math.floor(GemstonePrices.getPrice(key) * (collectedSinceStart / GemstonePrices.recipes.get(key)) / (uptime / 3600.0f)) : 0);
                    }
                    break;
                default:
                    logger.warn("[SCT]: Unknown collection type: {}", collectionType);
            }
        }

        // Update values
        collectionAmount = (float) Math.floor(currentCollection);
        collectionPerHour = uptime > 0 ? (float) Math.floor((collectedSinceStart / uptime) * 3600) : 0;
        collectionMade = (float) Math.floor(collectedSinceStart);
        moneyPerHourNPC = uptime > 0 ? (float) Math.floor(priceNPC * collectedSinceStart / (uptime / 3600.0f)) : 0;

        // Update overlay stats
        updateStats();
    }
}
