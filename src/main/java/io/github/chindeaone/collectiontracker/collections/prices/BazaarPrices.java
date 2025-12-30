package io.github.chindeaone.collectiontracker.collections.prices;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Iterator;
import java.util.Map;

public class BazaarPrices {

    public static float normalPrice = 0.0f;
    public static float enchantedPrice = 0.0f;
    public static float superEnchantedPrice = 0.0f;

    public static void setPrices(String json, String type) {
        resetPrices();

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        Iterator<Map.Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();

        if(type.equals("normal")) {
            if(iterator.hasNext()){
                Map.Entry<String, JsonElement> entry = iterator.next();
                normalPrice = entry.getValue().getAsFloat();
            }
        } else {
            if(iterator.hasNext()){
                Map.Entry<String, JsonElement> entry = iterator.next();
                enchantedPrice = entry.getValue().getAsFloat();
            }

            if(iterator.hasNext()){
                Map.Entry<String, JsonElement> entry = iterator.next();
                superEnchantedPrice = entry.getValue().getAsFloat();
            }
        }
    }

    private static void resetPrices() {
        normalPrice = 0.0f;
        enchantedPrice = 0.0f;
        superEnchantedPrice = 0.0f;
    }

}
