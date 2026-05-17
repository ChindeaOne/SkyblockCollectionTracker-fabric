package io.github.chindeaone.collectiontracker.tracker.collection;

import java.util.Collections;
import java.util.List;

public class LeaderboardManager {
    private static volatile List<LeaderboardEntry> currentLeaderboard = List.of();

    public static void set(List<LeaderboardEntry> entries) {
        currentLeaderboard = entries;
    }

    public static LeaderboardEntry getPlayerEntry() {
        List<LeaderboardEntry> lb = currentLeaderboard;
        if (lb.isEmpty()) {
            return null;
        }

        int index = findBinaryIndex(lb, TrackingRates.collectionAmount);
        if (index < lb.size()) {
            return lb.get(index);
        }

        return null;
    }

    public static LeaderboardEntry getNextRankEntry() {
        List<LeaderboardEntry> lb = currentLeaderboard;
        if (lb.isEmpty()) {
            return null;
        }

        int index = findBinaryIndex(lb, TrackingRates.collectionAmount);

        if (index > 0) {
            return lb.get(index - 1);
        }

        return null;
    }

    public static int findBinaryIndex(List<LeaderboardEntry> lb, long targetAmount) {
        int index = Collections.binarySearch(lb, new LeaderboardEntry("", 0, targetAmount),
                (a, b) -> Long.compare(b.amount(), a.amount()));

        if (index < 0) {
            index = -index - 1;
        }
        return index;
    }

    public static void clear() {
        currentLeaderboard = List.of();
    }

    public static boolean isEmpty() {
        return currentLeaderboard.isEmpty();
    }
}
