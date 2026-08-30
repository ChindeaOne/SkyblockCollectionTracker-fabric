package io.github.chindeaone.collectiontracker.tracker.collection

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.commands.CollectionTracker

object LeaderboardManager {
    @Volatile
    private var currentLeaderboard = mutableListOf<LeaderboardEntry>()
    @Volatile
    private var skillLeaderboards = mutableMapOf<String, List<LeaderboardEntry>>()

    @JvmStatic
    fun set(entries: MutableList<LeaderboardEntry>) {
        currentLeaderboard = entries
    }

    @JvmStatic
    fun setSkillLeaderboard(skill: String, entries: MutableList<LeaderboardEntry>) {
        skillLeaderboards[skill.lowercase()] = entries
    }

    private fun getPlayerEntryRaw(amount: Long): LeaderboardEntry? {
        val lb = currentLeaderboard
        if (lb.isEmpty()) {
            return null
        }

        val index = findBinaryIndex(lb, amount)
        if (index < lb.size) {
            return lb[index]
        }

        return null
    }

    fun getPlayerRank(amount: Long): Int {
        val lb = currentLeaderboard
        if (lb.isEmpty()) {
            return -1
        }

        return findBinaryIndex(lb, amount) + 1
    }

    fun getPlayerEntry(skill: String, amount: Long): LeaderboardEntry? {
        val lb = skillLeaderboards.getOrDefault(skill.lowercase(), listOf())
        if (lb.isEmpty()) return null

        val index = findBinaryIndex(lb, amount)
        return if (index < lb.size) lb[index] else null
    }

    @JvmStatic
    fun getPlayerRank(skill: String, amount: Long): Int {
        val lb = skillLeaderboards.getOrDefault(skill.lowercase(), listOf())
        if (lb.isEmpty()) {
            return -1
        }

        return findBinaryIndex(lb, amount) + 1
    }

    private fun getNextRankEntryRaw(amount: Long): LeaderboardEntry? {
        val lb = currentLeaderboard
        if (lb.isEmpty()) {
            return null
        }

        val index = findBinaryIndex(lb, amount)
        if (index > 0) {
            return lb[index - 1]
        }

        return null
    }

    private fun getPreviousRankEntryRaw(amount: Long): LeaderboardEntry? {
        val lb = currentLeaderboard
        if (lb.isEmpty()) {
            return null
        }

        val index = findBinaryIndex(lb, amount)
        if (index < lb.size - 1) {
            return lb[index + 1]
        }

        return null
    }

    private fun getNextRankEntryRaw(skill: String, amount: Long): LeaderboardEntry? {
        val lb = skillLeaderboards.getOrDefault(skill.lowercase(), listOf())
        if (lb.isEmpty()) {
            return null
        }

        val index = findBinaryIndex(lb, amount)
        if (index > 0) {
            return lb[index - 1]
        }

        return null
    }

    private fun  getPreviousRankEntryRaw(skill: String, amount: Long): LeaderboardEntry? {
        val lb = skillLeaderboards.getOrDefault(skill.lowercase(), listOf())
        if (lb.isEmpty()) {
            return null
        }

        val index = findBinaryIndex(lb, amount)
        if (index < lb.size - 1) {
            return lb[index + 1]
        }

        return null
    }

    fun getNextRankEntry(amount: Long): LeaderboardEntry? {
        val lb = currentLeaderboard
        if (lb.isEmpty()) {
            return null
        }

        // Custom position
        if (ConfigAccess.isCustomPositionEnabled() && !ConfigAccess.getCustomGoals().isEmpty()) {
            val position = ConfigAccess.getCustomPositionEntry("gemstone")

            if (position != null) {
                val playerEntry = getPlayerEntryRaw(amount)
                // default if player already passed the goal position
                if (playerEntry != null && playerEntry.rank < position) {
                    return getNextRankEntryRaw(amount)
                }
                return getEntryAtPosition(position)
            }
        }

        // Default
        return getNextRankEntryRaw(amount)
    }

    fun getNextRankEntry(skill: String, amount: Long): LeaderboardEntry? {
        val lb = skillLeaderboards.getOrDefault(skill.lowercase(), listOf())
        if (lb.isEmpty()) {
            return null
        }

        // Custom position
        if (ConfigAccess.isCustomPositionEnabled() && !ConfigAccess.getCustomGoals().isEmpty()) {
            val position = ConfigAccess.getCustomPositionEntry(skill)

            if (position != null) {
                val playerEntry = getPlayerEntry(skill, amount)
                // default if player already passed the goal position
                if (playerEntry != null && playerEntry.rank < position) {
                    return getNextRankEntryRaw(skill, amount)
                }
                return getSkillEntryAtPosition(skill, position)
            }
        }

        // Default
        return getNextRankEntryRaw(skill, amount)
    }

    fun getPreviousRankEntry(skill: String, amount: Long): LeaderboardEntry? {
        val lb = skillLeaderboards.getOrDefault(skill.lowercase(), listOf())
        if (lb.isEmpty()) {
            return null
        }

        return getPreviousRankEntryRaw(skill, amount)
    }

    fun getPreviousRankEntry(amount: Long): LeaderboardEntry? {
        val lb = currentLeaderboard
        if (lb.isEmpty()) {
            return null
        }

        return getPreviousRankEntryRaw(amount)
    }

    @JvmStatic
    fun getPlayerRank(): Int {
        return getPlayerRank(TrackingRates.collectionAmount)
    }

    @JvmStatic
    fun getNextRankEntry(): LeaderboardEntry? {
        // Custom position
        if (ConfigAccess.isCustomPositionEnabled() && !ConfigAccess.getCustomGoals().isEmpty()) {
            val position = ConfigAccess.getCustomPositionEntry(CollectionTracker.collection)

            if (position != null) {
                val playerEntry = getPlayerEntryRaw(TrackingRates.collectionAmount)
                // default if player already passed the goal position
                if (playerEntry != null && playerEntry.rank < position) {
                    return getNextRankEntryRaw(TrackingRates.collectionAmount)
                }
                return getEntryAtPosition(position)
            }
        }

        // Default
        return getNextRankEntryRaw(TrackingRates.collectionAmount)
    }

    @JvmStatic
    fun getPreviousRankEntry(): LeaderboardEntry? {
        return getPreviousRankEntryRaw(TrackingRates.collectionAmount)
    }

    fun getEntryAtPosition(position: Int): LeaderboardEntry? {
        val lb = currentLeaderboard

        if (lb.isEmpty() || position < 1 || position > lb.size) {
            return null
        }
        return lb[position - 1]
    }

    @JvmStatic
    fun getNextRankEntryForSkill(skill: String, skillXp: Long): LeaderboardEntry? {
        return getNextRankEntry(skill, skillXp)
    }

    @JvmStatic
    fun getPreviousRankEntryForSkill(skill: String, skillXp: Long): LeaderboardEntry? {
        return getPreviousRankEntry(skill, skillXp)
    }

    fun getSkillEntryAtPosition(skill: String, position: Int): LeaderboardEntry? {
        val lb = skillLeaderboards.getOrDefault(skill.lowercase(), listOf())
        if (lb.isEmpty() || position < 1 || position > lb.size) {
            return null
        }
        return lb[position - 1]
    }

    fun findBinaryIndex(lb: List<LeaderboardEntry>, targetAmount: Long): Int {
        var index = lb.binarySearch(
            LeaderboardEntry("", 0, targetAmount, ConfigAccess.isIncludeWipedProfilesEnabled()),
            { a, b -> b.amount.compareTo(a.amount) })

        if (index < 0) {
            index = -index - 1
        }
        return index
    }

    @JvmStatic
    fun clear() {
        currentLeaderboard = mutableListOf()
        skillLeaderboards.clear()
    }

    @JvmStatic
    fun isEmpty(): Boolean {
        return currentLeaderboard.isEmpty()
    }
}

data class LeaderboardEntry(var username: String, var rank: Int, var amount: Long, var wiped: Boolean)