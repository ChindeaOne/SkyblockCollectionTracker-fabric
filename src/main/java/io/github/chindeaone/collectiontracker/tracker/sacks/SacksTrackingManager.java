package io.github.chindeaone.collectiontracker.tracker.sacks;

import io.github.chindeaone.collectiontracker.tracker.TrackingRates;

public class SacksTrackingManager {

    public static void onChatCollection(long amount) {
        TrackingRates.calculateRates(amount, true);
    }
}
