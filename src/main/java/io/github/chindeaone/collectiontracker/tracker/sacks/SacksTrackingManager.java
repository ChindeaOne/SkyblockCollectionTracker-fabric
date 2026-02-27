package io.github.chindeaone.collectiontracker.tracker.sacks;

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager;
import io.github.chindeaone.collectiontracker.collections.CollectionsManager;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates;
import io.github.chindeaone.collectiontracker.utils.inventory.InventoryListener;

public class SacksTrackingManager {

    public static boolean lastHasItem = false;
    public static boolean isFirstCheck = true;

    public static void onChatCollection(int amount) {
        if (!TrackingHandler.isTracking) return;

        long updatedAmount = updateAmount(amount);
        TrackingRates.calculateRates(updatedAmount, true);
    }

    private static Long updateAmount(int amount) {
        String type = CollectionsManager.collectionType;

        long recipeSize;
        switch (type) {
            case "enchanted" -> recipeSize = BazaarCollectionsManager.enchantedRecipe.values().iterator().next();
            case "gemstone" -> recipeSize = 80;
            default -> {
                return (long) amount;
            }
        }

        boolean isCompacting = InventoryListener.isCompacting();
        boolean prevHasItem = lastHasItem;
        lastHasItem = isCompacting;

        boolean firstCheck = isFirstCheck;
        if (isFirstCheck) isFirstCheck = false;

        if (isCompacting) {
            // If the player has the item in their inventory, we need to check if they had it previously
            // Unless it's the first check, in which case we assume the amount is compacted
            if (!firstCheck && !prevHasItem) {
                return (long) amount;
            }
            // If the player had the item previously, we assume the amount is compacted
            return amount * recipeSize;
        } else {
            // Otherwise, the amount is not compacted and we can use it as is
            return (long) amount;
        }
    }

    public static void reset() {
        InventoryListener.setCompacting(false);
        InventoryListener.setCount(0);
        lastHasItem = false;
        isFirstCheck = true;
    }
}