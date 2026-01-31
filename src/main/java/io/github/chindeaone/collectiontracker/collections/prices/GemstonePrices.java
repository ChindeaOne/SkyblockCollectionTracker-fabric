package io.github.chindeaone.collectiontracker.collections.prices;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;

import java.util.HashMap;
import java.util.Map;

public class GemstonePrices {

    public static Map<String, Float> gemstonePrices = new HashMap<>();
    public static Map<String, Integer> recipes = new HashMap<>();

    public static void setPrices(String json) {
        Gson gson = new Gson();
        gemstonePrices.putAll(gson.fromJson(json, new TypeToken<Map<String, Float>>() {}.getType()));
        setRecipes();
        BazaarCollectionsManager.hasBazaarData = true;
    }

    private static void setRecipes() {
        for (String key : gemstonePrices.keySet()) {
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

    public static float getPrice(String gemstoneVariant) {
        return gemstonePrices.get(gemstoneVariant);
    }

    public static void resetPrices() {
        gemstonePrices.clear();
        recipes.clear();
    }
}