package io.github.chindeaone.collectiontracker.tracker.skills

import io.github.chindeaone.collectiontracker.commands.SkillTracker.skillName
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isSkillLeaderboardEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isTamingTrackingEnabled
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager.getNextRankEntryForSkill
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager.getPlayerRank
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager.getPreviousRankEntryForSkill
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.stopTracking
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.uptimeInSeconds
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import kotlin.concurrent.Volatile
import kotlin.math.floor

object SkillTrackingRates {
    @Volatile var skillLevel: Int = 0 // session start level and api level
    @Volatile var skillXp: Long = 0 // session start xp and api xp
    @Volatile var totalSkillXp: Long = 0
    @Volatile var skillXpGained: Long = 0L
    @Volatile var skillPerHour: Long = 0L

    private var lastXpGained = 0L
    var tamingLevel: Int = 0 // session start level and api level

    @Volatile var tamingXp: Long = 0 // session start xp and api xp
    @Volatile var tamingXpGained: Long = 0L
    @Volatile var tamingPerHour: Long = 0L

    private var lastTamingXpGained = 0L

    var afk: Boolean = false
    private var skillUnchangedStreak = 0
    private var tamingUnchangedStreak = 0
    private const val THRESHOLD = 2 // Number of checks before considering AFK

    // Skill Leaderboard tracking data
    @Volatile var currentSkillRank: Int = -1
    @Volatile var nextSkillRankUsername: String? = null
    @Volatile var nextSkillRankAmount: Long = -1L
    @Volatile var tillNextSkillRank: Long = -1L
    @Volatile var etaToNextSkillRank: String? = null
    @Volatile var isNextSkillWiped: Boolean = false
    @Volatile var previousSkillRankUsername: String? = null
    @Volatile var previousSkillRankAmount: Long = -1L
    @Volatile var abovePreviousSkillRankAmount: Long = -1L
    @Volatile var isPreviousSkillWiped: Boolean = false

    // Taming Leaderboard tracking data
    @Volatile var currentTamingRank: Int = -1
    @Volatile var nextTamingRankUsername: String? = null
    @Volatile var nextTamingRankAmount: Long = -1L
    @Volatile var tillNextTamingRank: Long = -1L
    @Volatile var etaToNextTamingRank: String? = null
    @Volatile var isNextTamingWiped: Boolean = false
    @Volatile var previousTamingRankUsername: String? = null
    @Volatile var previousTamingRankAmount: Long = -1L
    @Volatile var abovePreviousTamingRankAmount: Long = -1L
    @Volatile var isPreviousTamingWiped: Boolean = false

    fun initTracking(level: Int, xp: Long) {
        skillLevel = level
        skillXp = xp
        totalSkillXp = xp

        if (isTamingTrackingEnabled()) {
            tamingLevel = SkillUtils.tamingLevel
            tamingXp = SkillUtils.tamingValue.toLong()
        }
    }

    fun calculateSkillRates(value: Long) {
        skillXpGained = value - (skillXp - (if (SkillTrackingHandler.isSkillMaxed) SkillUtils.getMaxXpForSkill(skillName) else 0L)) // total gained since tracking started

        // AFK detection (API calls only)
        if (!SkillTrackingHandler.isSkillMaxed) {
            if (lastXpGained != skillXpGained) {
                lastXpGained = skillXpGained
                skillUnchangedStreak = 0
                afk = false
            } else {
                skillUnchangedStreak++
                if (skillUnchangedStreak >= THRESHOLD) {
                    afk = true
                    stopTracking()
                    return
                }
            }
        }
        val uptime = uptimeInSeconds
        skillPerHour = if (uptime > 0) floor(skillXpGained / (uptime / 3600.0)).toLong() else 0
        totalSkillXp = skillXp + skillXpGained

        updateSkillLeaderboardStats()
        updateSkillEta()
    }

    fun calculateTamingRates(value: Long) {
        tamingXpGained = value - tamingXp // total gained since tracking started

        // AFK detection (API calls only)
        if (lastTamingXpGained != tamingXpGained) {
            lastTamingXpGained = tamingXpGained
            tamingUnchangedStreak = 0
            afk = false
        } else {
            tamingUnchangedStreak++
            if (tamingUnchangedStreak >= THRESHOLD) {
                afk = true
                stopTracking()
                return
            }
        }

        val uptime = uptimeInSeconds
        tamingPerHour = if (uptime > 0) floor(tamingXpGained / (uptime / 3600.0)).toLong() else 0

        updateTamingLeaderboardStats()
        updateTamingEta()
    }

    fun updateSkillLeaderboardStats() {
        if (!isSkillLeaderboardEnabled()) return

        currentSkillRank = getPlayerRank(skillName, totalSkillXp)

        val nextEntry = getNextRankEntryForSkill(skillName, totalSkillXp)
        if (nextEntry != null) {
            nextSkillRankUsername = nextEntry.username
            nextSkillRankAmount = nextEntry.amount
            tillNextSkillRank = nextSkillRankAmount - totalSkillXp
            updateSkillEta()
            isNextSkillWiped = nextEntry.wiped
        } else {
            nextSkillRankUsername = null
            nextSkillRankAmount = -1L
            tillNextSkillRank = -1L
            etaToNextSkillRank = null
            isNextSkillWiped = false
        }

        val previousEntry = getPreviousRankEntryForSkill(skillName, totalSkillXp)
        if (previousEntry != null) {
            previousSkillRankUsername = previousEntry.username
            previousSkillRankAmount = previousEntry.amount
            abovePreviousSkillRankAmount = totalSkillXp - previousSkillRankAmount
            isPreviousSkillWiped = previousEntry.wiped
        } else {
            previousSkillRankUsername = null
            previousSkillRankAmount = -1L
            abovePreviousSkillRankAmount = -1L
            isPreviousSkillWiped = false
        }
    }

    fun updateTamingLeaderboardStats() {
        if (!isSkillLeaderboardEnabled() || !isTamingTrackingEnabled()) return

        currentTamingRank = getPlayerRank("Taming", tamingXp + tamingXpGained)

        val nextEntry = getNextRankEntryForSkill("Taming", tamingXp + tamingXpGained)
        if (nextEntry != null) {
            nextTamingRankUsername = nextEntry.username
            nextTamingRankAmount = nextEntry.amount
            tillNextTamingRank = nextTamingRankAmount - (tamingXp + tamingXpGained)
            updateTamingEta()
            isNextTamingWiped = nextEntry.wiped
        } else {
            nextTamingRankUsername = null
            nextTamingRankAmount = -1L
            tillNextTamingRank = -1L
            etaToNextTamingRank = null
            isNextTamingWiped = false
        }

        val previousEntry = getPreviousRankEntryForSkill("Taming", tamingXp + tamingXpGained)
        if (previousEntry != null) {
            previousTamingRankUsername = previousEntry.username
            previousTamingRankAmount = previousEntry.amount
            abovePreviousTamingRankAmount = (tamingXp + tamingXpGained) - previousTamingRankAmount
            isPreviousTamingWiped = previousEntry.wiped
        } else {
            previousTamingRankUsername = null
            previousTamingRankAmount = -1L
            abovePreviousTamingRankAmount = -1L
            isPreviousTamingWiped = false
        }
    }

    fun updateSkillEta() {
        if (skillPerHour > 0 && tillNextSkillRank > 0) {
            val seconds = (tillNextSkillRank / (skillPerHour / 3600.0)).toLong()
            etaToNextSkillRank = StringUtils.formatCompactTime(seconds)
        } else {
            etaToNextSkillRank = null
        }
    }

    fun updateTamingEta() {
        if (tamingPerHour > 0 && tillNextTamingRank > 0) {
            val seconds = (tillNextTamingRank / (tamingPerHour / 3600.0)).toLong()
            etaToNextTamingRank = StringUtils.formatCompactTime(seconds)
        } else {
            etaToNextTamingRank = null
        }
    }

    fun resetSession() {
        skillLevel = 0
        skillXp = 0L
        totalSkillXp = 0L
        skillXpGained = 0L
        skillPerHour = 0L

        tamingLevel = 0
        tamingXp = 0L
        tamingXpGained = 0L
        tamingPerHour = 0L

        lastXpGained = 0L
        lastTamingXpGained = 0L
        afk = false
        skillUnchangedStreak = 0
        tamingUnchangedStreak = 0

        currentSkillRank = -1
        nextSkillRankUsername = null
        nextSkillRankAmount = -1L
        tillNextSkillRank = -1L
        etaToNextSkillRank = null
        isNextSkillWiped = false
        previousSkillRankUsername = null
        previousSkillRankAmount = -1L
        abovePreviousSkillRankAmount = -1L
        isPreviousSkillWiped = false

        currentTamingRank = -1
        nextTamingRankUsername = null
        nextTamingRankAmount = -1L
        tillNextTamingRank = -1L
        etaToNextTamingRank = null
        isNextTamingWiped = false

        isPreviousTamingWiped = false
        previousTamingRankUsername = null
        previousTamingRankAmount = -1L
        abovePreviousTamingRankAmount = -1L
    }
}