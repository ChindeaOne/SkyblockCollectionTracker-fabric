package io.github.chindeaone.collectiontracker.tracker.collection;

import java.util.List;

public class LeaderboardManager {
    private static volatile List<LeaderboardEntry> currentLeaderboard = List.of();

    public static void updateLeaderboard(List<LeaderboardEntry> entries) {
        currentLeaderboard = List.copyOf(entries);
    }

    public static LeaderboardEntry getPlayerEntry() {
        List<LeaderboardEntry> lb = currentLeaderboard;
        if (lb.isEmpty()) return null;

        LeaderboardEntry current = null;
        for (LeaderboardEntry entry : lb) {
            if (TrackingRates.collectionAmount >= entry.amount()) {
                break;
            }
            current = entry;
        }
        return current;
    }

    public static LeaderboardEntry getNextRankEntry() {
        List<LeaderboardEntry> lb = currentLeaderboard;
        if (lb.isEmpty()) {
            return null;
        }

        LeaderboardEntry previous = null;

        for (LeaderboardEntry entry : lb) {
            if (TrackingRates.collectionAmount >= entry.amount()) {
                return previous;
            }
            previous = entry;
        }
        return null;
    }

    public static boolean shouldRefetch(long collectionAmount) {
        List<LeaderboardEntry> lb = currentLeaderboard;

        if (lb.isEmpty()) {
            return true;
        }
        LeaderboardEntry playerEntry = getPlayerEntry();
        if (playerEntry != null && playerEntry.rank() == 1) {
            return false;
        }

        return collectionAmount > lb.getFirst().amount();
    }

    public static void clear() {
        currentLeaderboard = List.of();
    }
}
