package io.github.chindeaone.collectiontracker.collections;

import java.util.*;

public class CollectionsManager {

    public static Map<String, Set<String>> collections = new HashMap<>();
    public static String collectionSource;
    public static String collectionType;

    public static List<String> multiCollectionSource = new LinkedList<>();
    public static Map<String, String> multiCollectionTypes = new HashMap<>();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValidCollection(String collectionName) {
        for (Set<String> collectionSet : collections.values()) {
            if (collectionSet.contains(collectionName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCollection(String collectionName) {
        for (Map.Entry<String, Set<String>> entry : collections.entrySet()) {
            if (!entry.getKey().equals("Miscellaneous") && entry.getValue().contains(collectionName)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getAllCollections() {
        List<String> allCollections = new LinkedList<>();
        for (Set<String> collectionSet : collections.values()) {
            allCollections.addAll(collectionSet);
        }
        return allCollections;
    }

    public static boolean isRiftCollection(String collectionName) {
        return collections
                .getOrDefault("Rift", Collections.emptySet())
                .contains(collectionName);
    }

    public static void resetCollections() {
        collectionSource = null;
        collectionType = null;
    }

    public static void resetMultiCollections() {
        multiCollectionSource.clear();
        multiCollectionTypes.clear();
    }
}