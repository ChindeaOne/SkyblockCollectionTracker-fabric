package io.github.chindeaone.collectiontracker.collections;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BazaarCollectionsManager {

    public static boolean hasBazaarData = false;

    public static final Map<String, Integer> enchantedRecipe = new HashMap<>();
    public static final Map<String, Integer> superEnchantedRecipe = new HashMap<>();

    public static void setPricesAndRecipes(String json, String type) {
        enchantedRecipe.clear();
        superEnchantedRecipe.clear();

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        if (!jsonObject.has("prices") && !jsonObject.has("recipe")) {
            BazaarPrices.setPrices(jsonObject.toString(), type);
        } else {

            JsonObject prices = jsonObject.getAsJsonObject("prices");
            JsonObject recipe = jsonObject.getAsJsonObject("recipe");

            Iterator<Map.Entry<String, JsonElement>> iterator = recipe.entrySet().iterator();

            if (iterator.hasNext()) {
                Map.Entry<String, JsonElement> entry = iterator.next();
                enchantedRecipe.put(entry.getKey(), entry.getValue().getAsInt());
            }

            if (iterator.hasNext()) {
                Map.Entry<String, JsonElement> entry = iterator.next();
                superEnchantedRecipe.put(entry.getKey(), entry.getValue().getAsInt());
            }
            BazaarPrices.setPrices(prices.toString(), type);
        }
        hasBazaarData = true;
    }
}
