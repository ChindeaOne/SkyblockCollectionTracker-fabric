package io.github.chindeaone.collectiontracker.tracker.collection;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.categories.overlay.LeaderboardConfig;
import io.github.chindeaone.collectiontracker.commands.CollectionTracker;

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

    private static LeaderboardEntry getPlayerEntryRaw(long amount) {
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

    public static LeaderboardEntry getPlayerEntry(long amount) {
        return getPlayerEntryRaw(amount);
    }

    public static LeaderboardEntry getPlayerEntry(String skill, long amount) {
        List<LeaderboardEntry> lb = skillLeaderboards.getOrDefault(skill.toLowerCase(), List.of());
        if (lb.isEmpty()) return null;

        int index = findBinaryIndex(lb, amount);
        return index < lb.size() ? lb.get(index) : null;
    }

    private static LeaderboardEntry getNextRankEntryRaw(long amount) {
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

    private static LeaderboardEntry getNextRankEntryRaw(String skill, long amount) {
        List<LeaderboardEntry> lb = skillLeaderboards.getOrDefault(skill.toLowerCase(), List.of());
        if (lb.isEmpty()) {
            return null;
        }

        int index = findBinaryIndex(lb, amount);


        if (index > 0) {
            return lb.get(index - 1);
        }

        return null;
    }

    public static LeaderboardEntry getNextRankEntry(long amount) {
        List<LeaderboardEntry> lb = currentLeaderboard;
        if (lb.isEmpty()) {
            return null;
        }

        // Custom goal
        if (ConfigAccess.isCustomGoalEnabled() && !ConfigAccess.getCustomGoals().isEmpty()) {
            LeaderboardConfig.CustomGoalEntry goalEntry = ConfigAccess.getCustomGoalEntry("gemstone");

            if (goalEntry != null) {
                LeaderboardConfig.CustomGoalType goalType = ConfigAccess.getCustomGoalType();

                if (goalType == LeaderboardConfig.CustomGoalType.POSITION && goalEntry.position != null) {
                    LeaderboardEntry playerEntry = getPlayerEntryRaw(amount);
                    // default if player already passed the goal position
                    if (playerEntry != null && playerEntry.rank() < goalEntry.position) {
                        return getNextRankEntryRaw(amount);
                    }
                    return getEntryAtPosition(goalEntry.position);
                } else if (goalType == LeaderboardConfig.CustomGoalType.AMOUNT && goalEntry.amount != null) {
                    LeaderboardEntry playerEntry = getPlayerEntryRaw(amount);
                    // default if player's collection is greater than the custom goal amount
                    if (playerEntry != null && amount > goalEntry.amount) {
                        return getNextRankEntryRaw(amount);
                    }
                    LeaderboardEntry entry = getNextRankEntryRaw(goalEntry.amount);
                    // If goal amount exceeds all players, return custom goal entry
                    if (entry == null && !lb.isEmpty()) {
                        return new LeaderboardEntry("Custom Goal", 0, goalEntry.amount);
                    }
                    return entry;
                }
            }
        }

        // Default
        return getNextRankEntryRaw(amount);
    }

    public static LeaderboardEntry getNextRankEntry(String skill, long amount) {
        List<LeaderboardEntry> lb = skillLeaderboards.getOrDefault(skill.toLowerCase(), List.of());
        if (lb.isEmpty()) return null;

        // Custom goal
        if (ConfigAccess.isCustomGoalEnabled() && !ConfigAccess.getCustomGoals().isEmpty()) {
            LeaderboardConfig.CustomGoalEntry goalEntry = ConfigAccess.getCustomGoalEntry(skill);

            if (goalEntry != null) {
                LeaderboardConfig.CustomGoalType goalType = ConfigAccess.getCustomGoalType();

                if (goalType == LeaderboardConfig.CustomGoalType.POSITION && goalEntry.position != null) {
                    LeaderboardEntry playerEntry = getPlayerEntry(skill, amount);
                    // default if player already passed the goal position
                    if (playerEntry != null && playerEntry.rank() < goalEntry.position) {
                        return getNextRankEntryRaw(skill, amount);
                    }
                    return getSkillEntryAtPosition(skill, goalEntry.position);
                } else if (goalType == LeaderboardConfig.CustomGoalType.AMOUNT && goalEntry.amount != null) {
                    LeaderboardEntry playerEntry = getPlayerEntry(skill, amount);
                    // default if player's skill xp is greater than the custom goal amount
                    if (playerEntry != null && amount > goalEntry.amount) {
                        return getNextRankEntryRaw(skill, amount);
                    }
                    LeaderboardEntry entry = getNextRankEntryRaw(skill, goalEntry.amount);
                    // If goal amount exceeds all players, return custom goal entry
                    if (entry == null && !lb.isEmpty()) {
                        return new LeaderboardEntry("Custom Goal", 0, goalEntry.amount);
                    }
                    return entry;
                }
            }
        }

        // Default
        return getNextRankEntryRaw(skill, amount);
    }

    public static LeaderboardEntry getPlayerEntry() {
        return getPlayerEntryRaw(TrackingRates.collectionAmount);
    }

    public static LeaderboardEntry getNextRankEntry() {
        // Custom goal
        if (ConfigAccess.isCustomGoalEnabled() && !ConfigAccess.getCustomGoals().isEmpty()) {
            LeaderboardConfig.CustomGoalEntry goalEntry = ConfigAccess.getCustomGoalEntry(CollectionTracker.collection);

            if (goalEntry != null) {
                LeaderboardConfig.CustomGoalType goalType = ConfigAccess.getCustomGoalType();

                if (goalType == LeaderboardConfig.CustomGoalType.POSITION && goalEntry.position != null) {
                    LeaderboardEntry playerEntry = getPlayerEntryRaw(TrackingRates.collectionAmount);
                    // default if player already passed the goal position
                    if (playerEntry != null && playerEntry.rank() < goalEntry.position) {
                        return getNextRankEntryRaw(TrackingRates.collectionAmount);
                    }
                    return getEntryAtPosition(goalEntry.position);
                } else if (goalType == LeaderboardConfig.CustomGoalType.AMOUNT && goalEntry.amount != null) {
                    LeaderboardEntry playerEntry = getPlayerEntryRaw(TrackingRates.collectionAmount);
                    // default if player's collection is greater than the custom goal amount
                    if (playerEntry != null && TrackingRates.collectionAmount > goalEntry.amount) {
                        return getNextRankEntryRaw(TrackingRates.collectionAmount);
                    }
                    LeaderboardEntry entry = getNextRankEntryRaw(goalEntry.amount);
                    // If goal amount exceeds all players, return custom goal entry
                    if (entry == null && !currentLeaderboard.isEmpty()) {
                        return new LeaderboardEntry("Custom Goal", 0, goalEntry.amount);
                    }
                    return entry;
                }
            }
        }

        // Default
        return getNextRankEntryRaw(TrackingRates.collectionAmount);
    }

    public static LeaderboardEntry getEntryAtPosition(int position) {
        List<LeaderboardEntry> lb = currentLeaderboard;
        if (lb.isEmpty() || position < 1 || position > lb.size()) {
            return null;
        }
        return lb.get(position - 1);
    }

    public static LeaderboardEntry getNextRankEntryForSkill(String skill, long skillXp) {
        return getNextRankEntry(skill, skillXp);
    }

    public static LeaderboardEntry getSkillEntryAtPosition(String skill, int position) {
        List<LeaderboardEntry> lb = skillLeaderboards.getOrDefault(skill.toLowerCase(), List.of());
        if (lb.isEmpty() || position < 1 || position > lb.size()) {
            return null;
        }
        return lb.get(position - 1);
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
