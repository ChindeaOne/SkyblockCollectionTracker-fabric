package io.github.chindeaone.collectiontracker.tracker.sacks;

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates;

public class SacksTrackingManager {

    public static void onChatCollection(int amount, int timeframe) {
        if (!TrackingHandler.isTracking) return;

        long updatedAmount = updateAmount(amount, timeframe);
        TrackingRates.calculateRates(updatedAmount, true);
    }

    private static Long updateAmount(int amount, int timeframe) {
        String type = CollectionsManager.collectionType;

        long recipeSize;
        switch (type) {
            case "enchanted" -> recipeSize = BazaarCollectionsManager.enchantedRecipe.values().iterator().next();
            case "gemstone" -> recipeSize = 80;
            default -> {
                return (long) amount;
            }
        }
        // If the interval is short, assume raw collection
        if (timeframe < 10) {
            return (long) amount;
        }

        long upperBound = recipeSize * 3L; // Very work in progress upper bound, could be lower
        long lowerBound = recipeSize / 20L; // Very work in progress lower bound, could be higher

        if (amount <= upperBound && amount >= lowerBound) {
            return amount * recipeSize;
        }

        return (long) amount; // Fallback to raw amount if unsure to avoid inflating rates
    }
}