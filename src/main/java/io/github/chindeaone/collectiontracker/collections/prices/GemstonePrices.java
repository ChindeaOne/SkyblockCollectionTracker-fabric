package io.github.chindeaone.collectiontracker.collections.prices;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;

import java.util.HashMap;
import java.util.Map;

public class GemstonePrices {

    public static Map<String, Float> gemstoneInstantBuyPrices = new HashMap<>();
    public static Map<String, Float> gemstoneInstantSellPrices = new HashMap<>();
    public static Map<String, Integer> recipes = new HashMap<>();

    public static Map<String, Map<String, Float>> multiGemstoneInstantBuyPrices = new HashMap<>();
    public static Map<String, Map<String, Float>> multiGemstoneInstantSellPrices = new HashMap<>();
    public static Map<String, Map<String, Integer>> multiGemstoneRecipes = new HashMap<>();

    public static void setPrices(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonObject instantSell = jsonObject.getAsJsonObject("INSTANT_SELL");
        JsonObject instantBuy = jsonObject.getAsJsonObject("INSTANT_BUY");

        gemstoneInstantSellPrices.clear();
        for (Map.Entry<String, JsonElement> entry : instantSell.entrySet()) {
            gemstoneInstantSellPrices.put(entry.getKey(), entry.getValue().getAsFloat());
        }

        gemstoneInstantBuyPrices.clear();
        for (Map.Entry<String, JsonElement> entry : instantBuy.entrySet()) {
            gemstoneInstantBuyPrices.put(entry.getKey(), entry.getValue().getAsFloat());
        }

        setRecipes();
        BazaarCollectionsManager.hasBazaarData = true;
    }

    public static void setPrices(String collection, String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonObject instantSell = jsonObject.getAsJsonObject("INSTANT_SELL");
        JsonObject instantBuy = jsonObject.getAsJsonObject("INSTANT_BUY");

        Map<String, Float> sellPrices = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : instantSell.entrySet()) {
            sellPrices.put(entry.getKey(), entry.getValue().getAsFloat());
        }
        multiGemstoneInstantSellPrices.put(collection, sellPrices);

        Map<String, Float> buyPrices = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : instantBuy.entrySet()) {
            buyPrices.put(entry.getKey(), entry.getValue().getAsFloat());
        }
        multiGemstoneInstantBuyPrices.put(collection, buyPrices);

        setRecipes(collection, sellPrices);
        BazaarCollectionsManager.hasBazaarData = true;
    }

    private static void setRecipes() {
        recipes.clear();
        for (String key : gemstoneInstantSellPrices.keySet()) {
            int amount = 0;
            if (key.contains("ROUGH")) {
                amount = 1;
            } else if (key.contains("FLAWED")) {
                amount = 80;
            } else if (key.contains("FINE")) {
                amount = 80 * 80;
            } else if (key.contains("FLAWLESS")) {
                amount = 80 * 80 * 80;
            } else if (key.contains("PERFECT")) {
                amount = 5 * 80 * 80 * 80;
            }
            recipes.put(key, amount);
        }
    }

    private static void setRecipes(String collection, Map<String, Float> sellPrices) {
        Map<String, Integer> recipesPerColl = new HashMap<>();
        for (String key : sellPrices.keySet()) {
            int amount = 0;
            if (key.contains("ROUGH")) {
                amount = 1;
            } else if (key.contains("FLAWED")) {
                amount = 80;
            } else if (key.contains("FINE")) {
                amount = 80 * 80;
            } else if (key.contains("FLAWLESS")) {
                amount = 80 * 80 * 80;
            } else if (key.contains("PERFECT")) {
                amount = 5 * 80 * 80 * 80;
            }

            if (amount != 0) {
                recipesPerColl.put(key, amount);
            }
        }
        multiGemstoneRecipes.put(collection, recipesPerColl);
    }

    public static float getInstantBuyPrice(String gemstoneVariant) {
        return gemstoneInstantBuyPrices.getOrDefault(gemstoneVariant, 0.0f);
    }

    public static float getInstantSellPrice(String gemstoneVariant) {
        return gemstoneInstantSellPrices.getOrDefault(gemstoneVariant, 0.0f);
    }

    public static void resetPrices() {
        gemstoneInstantBuyPrices.clear();
        gemstoneInstantSellPrices.clear();
        recipes.clear();
        multiGemstoneInstantBuyPrices.clear();
        multiGemstoneInstantSellPrices.clear();
        multiGemstoneRecipes.clear();
    }
}