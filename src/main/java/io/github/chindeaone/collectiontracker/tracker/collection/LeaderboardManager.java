package io.github.chindeaone.collectiontracker.tracker.collection;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardManager {
    private static volatile List<LeaderboardEntry> currentLeaderboard = List.of();
    private static final Map<String, List<LeaderboardEntry>> skillLeaderboards = new ConcurrentHashMap<>();

    public static void set(List<LeaderboardEntry> entries) {
        currentLeaderboard = entries;
    }

    public static void setSkillLeaderboard(String skill, List<LeaderboardEntry> entries) {
        if (skill == null || entries == null) return;
        skillLeaderboards.put(skill.toLowerCase(), entries);
    }

    public static LeaderboardEntry getPlayerEntry(long amount) {
        List<LeaderboardEntry> lb = currentLeaderboard;
        if (lb.isEmpty()) {
            return null;
        }

        int index = findBinaryIndex(lb, amount);
        if (index < lb.size()) {
            return lb.get(index);
        }

        return null;
    }

    public static LeaderboardEntry getPlayerEntry(String skill, long amount) {
        List<LeaderboardEntry> lb = skillLeaderboards.getOrDefault(skill.toLowerCase(), List.of());
        if (lb.isEmpty()) return null;

        int index = findBinaryIndex(lb, amount);
        return index < lb.size() ? lb.get(index) : null;
    }

    public static LeaderboardEntry getNextRankEntry(long amount) {
        List<LeaderboardEntry> lb = currentLeaderboard;
        if (lb.isEmpty()) {
            return null;
        }

        int index = findBinaryIndex(lb, amount);

        if (index > 0) {
            return lb.get(index - 1);
        }

        return null;
    }

    public static LeaderboardEntry getNextRankEntry(String skill, long amount) {
        List<LeaderboardEntry> lb = skillLeaderboards.getOrDefault(skill.toLowerCase(), List.of());
        if (lb.isEmpty()) return null;

        int index = findBinaryIndex(lb, amount);
        return index > 0 ? lb.get(index - 1) : null;
    }

    public static LeaderboardEntry getPlayerEntry() {
        return getPlayerEntry(TrackingRates.collectionAmount);
    }

    public static LeaderboardEntry getNextRankEntry() {
        return getNextRankEntry(TrackingRates.collectionAmount);
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
        skillLeaderboards.clear();
    }

    public static boolean isEmpty() {
        return currentLeaderboard.isEmpty();
    }
}
