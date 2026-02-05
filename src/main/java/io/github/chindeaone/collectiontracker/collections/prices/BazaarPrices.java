package io.github.chindeaone.collectiontracker.collections.prices;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Iterator;
import java.util.Map;

public class BazaarPrices {

    public static float normalInstantBuy = 0.0f;
    public static float normalInstantSell = 0.0f;
    public static float enchantedInstantBuy = 0.0f;
    public static float enchantedInstantSell = 0.0f;
    public static float superEnchantedInstantBuy = 0.0f;
    public static float superEnchantedInstantSell = 0.0f;

    public static void setPrices(String json, String type) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        if (type.equals("normal")) {
            // normal: { "ITEM_ID": { "INSTANT_BUY": 25498.465, "INSTANT_SELL": 24380.9 } }
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                JsonObject prices = entry.getValue().getAsJsonObject();
                normalInstantBuy = prices.get("INSTANT_BUY").getAsFloat();
                normalInstantSell = prices.get("INSTANT_SELL").getAsFloat();
                break; // Should only be one item
            }
        } else if (type.equals("enchanted")) {
            // enchanted: { "INSTANT_SELL": { "ENCHANTED_GOLD": 486.3, "ENCHANTED_GOLD_BLOCK": 83085.2 }, "INSTANT_BUY": { ... } }
            JsonObject instantSell = jsonObject.getAsJsonObject("INSTANT_SELL");
            JsonObject instantBuy = jsonObject.getAsJsonObject("INSTANT_BUY");

            Iterator<Map.Entry<String, JsonElement>> sellIterator = instantSell.entrySet().iterator();
            Iterator<Map.Entry<String, JsonElement>> buyIterator = instantBuy.entrySet().iterator();

            if (sellIterator.hasNext()) {
                enchantedInstantSell = sellIterator.next().getValue().getAsFloat();
            }
            if (buyIterator.hasNext()) {
                enchantedInstantBuy = buyIterator.next().getValue().getAsFloat();
            }

            if (sellIterator.hasNext()) {
                superEnchantedInstantSell = sellIterator.next().getValue().getAsFloat();
            }
            if (buyIterator.hasNext()) {
                superEnchantedInstantBuy = buyIterator.next().getValue().getAsFloat();
            }
        }
    }

    public static void resetPrices() {
        normalInstantBuy = 0.0f;
        normalInstantSell = 0.0f;
        enchantedInstantBuy = 0.0f;
        enchantedInstantSell = 0.0f;
        superEnchantedInstantBuy = 0.0f;
        superEnchantedInstantSell = 0.0f;
    }
}