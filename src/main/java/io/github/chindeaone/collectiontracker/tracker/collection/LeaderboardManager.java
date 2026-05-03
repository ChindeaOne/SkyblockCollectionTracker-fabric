package io.github.chindeaone.collectiontracker.tracker.collection;

import io.github.chindeaone.collectiontracker.utils.PlayerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardManager {
    private static final List<LeaderboardEntry> currentLeaderboard = Collections.synchronizedList(new ArrayList<>());

    public static void updateLeaderboard(List<LeaderboardEntry> entries) {
        currentLeaderboard.clear();
        currentLeaderboard.addAll(entries);
    }

    public static LeaderboardEntry getPlayerEntry() {
        String currentPlayer = PlayerData.INSTANCE.getPlayerName();
        synchronized (currentLeaderboard) {
            for (LeaderboardEntry entry : currentLeaderboard) {
                if (currentPlayer.equalsIgnoreCase(entry.getUsername())) {
                    return entry;
                }
            }
        }
        return null;
    }

    public static LeaderboardEntry getNextRankEntry() {
        LeaderboardEntry playerEntry = getPlayerEntry();
        synchronized (currentLeaderboard) {
            if (playerEntry == null) {
                // If player is not in leaderboard, next rank is the last entry in leaderboard
                if (!currentLeaderboard.isEmpty()) {
                    return currentLeaderboard.getLast();
                }
                return null;
            }

            LeaderboardEntry prevEntry = null;
            for (LeaderboardEntry entry : currentLeaderboard) {
                if (playerEntry.getRank() == entry.getRank()) {
                    return prevEntry;
                }
                prevEntry = entry;
            }
        }
        return null;
    }

    public static void clear() {
        currentLeaderboard.clear();
    }

    public static boolean shouldRefetch(long collectionAmount) {
        if (currentLeaderboard.isEmpty()) return true;
        return collectionAmount > currentLeaderboard.getFirst().getAmount();
    }
}
