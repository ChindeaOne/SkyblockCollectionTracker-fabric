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
    @Volatile var skillCurrentRank: Int = -1
    @Volatile var skillNextRankUsername: String? = null
    @Volatile var skillNextRankAmount: Long = -1L
    @Volatile var skillTillNextRank: Long = -1L
    @Volatile var skillEtaToNextRank: String? = null
    @Volatile var isNextSkillWiped: Boolean = false
    @Volatile var skillPreviousRankUsername: String? = null
    @Volatile var skillPreviousRankAmount: Long = -1L
    @Volatile var skillAbovePreviousRankAmount: Long = -1L
    @Volatile var isPreviousSkillWiped: Boolean = false

    // Taming Leaderboard tracking data
    @Volatile var tamingCurrentRank: Int = -1
    @Volatile var tamingNextRankUsername: String? = null
    @Volatile var tamingNextRankAmount: Long = -1L
    @Volatile var tamingTillNextRank: Long = -1L
    @Volatile var tamingEtaToNextRank: String? = null
    @Volatile var isNextTamingWiped: Boolean = false
    @Volatile var tamingPreviousRankUsername: String? = null
    @Volatile var tamingPreviousRankAmount: Long = -1L
    @Volatile var tamingAbovePreviousRankAmount: Long = -1L
    @Volatile var isPreviousTamingWiped: Boolean = false

    fun initTracking(level: Int, xp: Long) {
        skillLevel = level
        skillXp = xp
        totalSkillXp = xp

        if (isTamingTrackingEnabled()) {
            tamingLevel = SkillUtils.getTamingLevel()
            tamingXp = SkillUtils.getTamingValue().toLong()
        }
    }

    @Synchronized
    fun calculateSkillRates(value: Long) {
        skillXpGained =
            value - (skillXp - (if (SkillTrackingHandler.isSkillMaxed) SkillUtils.getMaxXpForSkill(skillName) else 0L)) // total gained since tracking started

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

    @Synchronized
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

        skillCurrentRank = getPlayerRank(skillName, totalSkillXp)

        val nextEntry = getNextRankEntryForSkill(skillName, totalSkillXp)
        if (nextEntry != null) {
            skillNextRankUsername = nextEntry.username
            skillNextRankAmount = nextEntry.amount
            skillTillNextRank = skillNextRankAmount - totalSkillXp
            updateSkillEta()
            isNextSkillWiped = nextEntry.wiped
        } else {
            skillNextRankUsername = null
            skillNextRankAmount = -1L
            skillTillNextRank = -1L
            skillEtaToNextRank = null
            isNextSkillWiped = false
        }

        val previousEntry = getPreviousRankEntryForSkill(skillName, totalSkillXp)
        if (previousEntry != null) {
            skillPreviousRankUsername = previousEntry.username
            skillPreviousRankAmount = previousEntry.amount
            skillAbovePreviousRankAmount = totalSkillXp - skillPreviousRankAmount
            isPreviousSkillWiped = previousEntry.wiped
        } else {
            skillPreviousRankUsername = null
            skillPreviousRankAmount = -1L
            skillAbovePreviousRankAmount = -1L
            isPreviousSkillWiped = false
        }
    }

    fun updateTamingLeaderboardStats() {
        if (!isSkillLeaderboardEnabled() || !isTamingTrackingEnabled()) return

        tamingCurrentRank = getPlayerRank("Taming", tamingXp + tamingXpGained)

        val nextEntry = getNextRankEntryForSkill("Taming", tamingXp + tamingXpGained)
        if (nextEntry != null) {
            tamingNextRankUsername = nextEntry.username
            tamingNextRankAmount = nextEntry.amount
            tamingTillNextRank = tamingNextRankAmount - (tamingXp + tamingXpGained)
            updateTamingEta()
            isNextTamingWiped = nextEntry.wiped
        } else {
            tamingNextRankUsername = null
            tamingNextRankAmount = -1L
            tamingTillNextRank = -1L
            tamingEtaToNextRank = null
            isNextTamingWiped = false
        }

        val previousEntry = getPreviousRankEntryForSkill("Taming", tamingXp + tamingXpGained)
        if (previousEntry != null) {
            tamingPreviousRankUsername = previousEntry.username
            tamingPreviousRankAmount = previousEntry.amount
            tamingAbovePreviousRankAmount = (tamingXp + tamingXpGained) - tamingPreviousRankAmount
            isPreviousTamingWiped = previousEntry.wiped
        } else {
            tamingPreviousRankUsername = null
            tamingPreviousRankAmount = -1L
            tamingAbovePreviousRankAmount = -1L
            isPreviousTamingWiped = false
        }
    }

    fun updateSkillEta() {
        if (skillPerHour > 0 && skillTillNextRank > 0) {
            val seconds = (skillTillNextRank / (skillPerHour / 3600.0)).toLong()
            skillEtaToNextRank = StringUtils.formatCompactTime(seconds)
        } else {
            skillEtaToNextRank = null
        }
    }

    fun updateTamingEta() {
        if (tamingPerHour > 0 && tamingTillNextRank > 0) {
            val seconds = (tamingTillNextRank / (tamingPerHour / 3600.0)).toLong()
            tamingEtaToNextRank = StringUtils.formatCompactTime(seconds)
        } else {
            tamingEtaToNextRank = null
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

        skillCurrentRank = -1
        skillNextRankUsername = null
        skillNextRankAmount = -1L
        skillTillNextRank = -1L
        skillEtaToNextRank = null

        tamingCurrentRank = -1
        tamingNextRankUsername = null
        tamingNextRankAmount = -1L
        tamingTillNextRank = -1L
        tamingEtaToNextRank = null
    }
}