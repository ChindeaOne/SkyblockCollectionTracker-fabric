package io.github.chindeaone.collectiontracker.tracker.collection;

public class LeaderboardEntry {
    private final String username;
    private final int rank;
    private final long amount;

    public LeaderboardEntry(String username, int rank, long amount) {
        this.username = username;
        this.rank = rank;
        this.amount = amount;
    }

    public String getUsername() {
        return username;
    }

    public int getRank() {
        return rank;
    }

    public long getAmount() {
        return amount;
    }
}
