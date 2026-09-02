package io.github.chindeaone.collectiontracker.collections.prices;

import java.util.HashMap;
import java.util.Map;

public class NpcPrices {

    public static final Map<String, Integer> collectionPrices = new HashMap<>();

    public static int getNpcPrice(String collection) {
        return collectionPrices.getOrDefault(collection, -1);
    }
}