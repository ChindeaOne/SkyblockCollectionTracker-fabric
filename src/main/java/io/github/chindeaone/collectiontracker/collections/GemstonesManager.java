package io.github.chindeaone.collectiontracker.collections;


public class GemstonesManager {

    public static String[] gemstones;

    public static boolean checkIfGemstone(String collectionName) {
        for(String gemstone : gemstones) {
            if (collectionName.equalsIgnoreCase(gemstone)) {
                return true;
            }
        }
        return false;
    }
}