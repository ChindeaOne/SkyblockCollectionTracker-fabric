package io.github.chindeaone.collectiontracker.collections;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices;
import io.github.chindeaone.collectiontracker.collections.prices.GemstonePrices;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BazaarCollectionsManager {

    public static boolean hasBazaarData = false;

    public static final Map<String, Integer> enchantedRecipe = new HashMap<>();
    public static final Map<String, Integer> superEnchantedRecipe = new HashMap<>();

    public static final Map<String, Map<String, Integer>> multiEnchantedRecipes = new HashMap<>();
    public static final Map<String, Map<String, Integer>> multiSuperEnchantedRecipes = new HashMap<>();

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

    public static void setPricesAndRecipes(String collection, String json, String type) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        if (jsonObject.has("prices") || jsonObject.has("recipe")) {
            JsonObject prices = jsonObject.getAsJsonObject("prices");
            JsonObject recipe = jsonObject.getAsJsonObject("recipe");

            Map<String, Integer> enchanted = new HashMap<>();
            Map<String, Integer> superEnchanted = new HashMap<>();

            Iterator<Map.Entry<String, JsonElement>> iterator = recipe.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<String, JsonElement> entry = iterator.next();
                enchanted.put(entry.getKey(), entry.getValue().getAsInt());
            }
            if (iterator.hasNext()) {
                Map.Entry<String, JsonElement> entry = iterator.next();
                superEnchanted.put(entry.getKey(), entry.getValue().getAsInt());
            }

            multiEnchantedRecipes.put(collection, enchanted);
            multiSuperEnchantedRecipes.put(collection, superEnchanted);
            BazaarPrices.setPrices(collection, prices.toString(), type);
        } else {
            BazaarPrices.setPrices(collection, jsonObject.toString(), type);
        }
        hasBazaarData = true;
    }

    public static void resetBazaarData() {
        hasBazaarData = false;
        enchantedRecipe.clear();
        superEnchantedRecipe.clear();
        multiEnchantedRecipes.clear();
        multiSuperEnchantedRecipes.clear();
        BazaarPrices.resetPrices();
        GemstonePrices.resetPrices();
    }
}