package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.config.enableTamingTracking
import io.github.chindeaone.collectiontracker.config.leaderboardPosition
import io.github.chindeaone.collectiontracker.config.previousPosition
import io.github.chindeaone.collectiontracker.config.skillLeaderboard
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingRates
import io.github.chindeaone.collectiontracker.utils.NumbersUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils

object SkillParser {

    fun updateSkillLines(list: MutableList<String>) {
        val currentSkill = SkillTracker.skillName
        val currentSkillRank = SkillTrackingRates.currentSkillRank
        val leaderboard = skillLeaderboard
        val rankSuffix = formatRankSuffix(currentSkillRank, leaderboard)

        list.add("$currentSkill Level: ${NumbersUtils.formatNumber(SkillTrackingRates.skillLevel.toLong())}$rankSuffix")
        list.add("Total $currentSkill XP: ${StringUtils.formatNumber(SkillTrackingRates.totalSkillXp)}")
        list.add("XP (Session): ${StringUtils.formatNumber(SkillTrackingRates.skillXpGained)}")
        list.add("XP/h: ${StringUtils.formatNumber(SkillTrackingRates.skillPerHour)}")

        addLeaderboardLines(
            list,
            currentSkillRank,
            SkillTrackingRates.nextSkillRankUsername,
            SkillTrackingRates.nextSkillRankAmount,
            SkillTrackingRates.tillNextSkillRank,
            SkillTrackingRates.etaToNextSkillRank,
            SkillTrackingRates.isNextSkillWiped,
            SkillTrackingRates.previousSkillRankUsername,
            SkillTrackingRates.previousSkillRankAmount,
            SkillTrackingRates.abovePreviousSkillRankAmount,
            SkillTrackingRates.isPreviousSkillWiped,
            leaderboard
        )
        list.add("")
        list.add("Uptime: ${ if (SkillTrackingHandler.isPaused) "${SkillTrackingHandler.uptime} (§7Paused)" else SkillTrackingHandler.uptime}")
    }

    fun updateTamingLines(list: MutableList<String>) {
        val currentSkill = SkillTracker.skillName
        val withTaming = enableTamingTracking && currentSkill != "Taming"
        if (!withTaming) return

        val currentTamingTotalXp = SkillTrackingRates.tamingXp + SkillTrackingRates.tamingXpGained
        val currentTamingRank = SkillTrackingRates.currentTamingRank
        val leaderboard = skillLeaderboard
        val tamingRankSuffix = formatRankSuffix(currentTamingRank, leaderboard)

        list.add("")
        list.add("Taming Level: ${NumbersUtils.formatNumber(SkillTrackingRates.tamingLevel.toLong())}$tamingRankSuffix")
        list.add("Total Taming XP: ${StringUtils.formatNumber(currentTamingTotalXp)}")
        list.add("XP (Session): ${StringUtils.formatNumber(SkillTrackingRates.tamingXpGained)}")
        list.add("XP/h: ${StringUtils.formatNumber(SkillTrackingRates.tamingPerHour)}")

        addLeaderboardLines(
            list,
            currentTamingRank,
            SkillTrackingRates.nextTamingRankUsername,
            SkillTrackingRates.nextTamingRankAmount,
            SkillTrackingRates.tillNextTamingRank,
            SkillTrackingRates.etaToNextTamingRank,
            SkillTrackingRates.isNextTamingWiped,
            SkillTrackingRates.previousTamingRankUsername,
            SkillTrackingRates.previousTamingRankAmount,
            SkillTrackingRates.abovePreviousTamingRankAmount,
            SkillTrackingRates.isPreviousTamingWiped,
            leaderboard
        )
    }

    private fun formatRankSuffix(rank: Int, leaderboardEnabled: Boolean): String {
        if (!leaderboardEnabled || rank == -1) return ""
        return if (rank == 10001) " [Too low]" else " [#$rank]"
    }

    private fun addLeaderboardLines(
        list: MutableList<String>,
        rank: Int,
        nextUser: String?,
        nextAmount: Long,
        tillNext: Long,
        eta: String?,
        isNextWiped: Boolean,
        previousUser: String?,
        previousAmount: Long,
        abovePrevious: Long,
        isPreviousWiped: Boolean,
        leaderboardEnabled: Boolean
    ) {
        if (!leaderboardEnabled) return

        if (rank != 1) {
            val customPos = leaderboardPosition
            val posLabel = if (customPos) "Custom Position" else "Next Position"
            val tillLabel = if (customPos) "Till Custom Position" else "Till Next Position"
            val etaLabel = if (customPos) "ETA to Custom Position" else "ETA"

            list.add("")

            if (nextUser != null) {
                val wipedSuffix = if (isNextWiped) "-wiped" else ""
                list.add("$posLabel ($nextUser$wipedSuffix): ${NumbersUtils.formatNumber(nextAmount)}")
                if (tillNext == -1L) {
                    list.add("$tillLabel: Calculating...")
                } else {
                    list.add("$tillLabel: ${NumbersUtils.formatNumber(tillNext)}")
                }
                if (!eta.isNullOrEmpty()) {
                    list.add("$etaLabel: $eta")
                } else {
                    list.add("$etaLabel: Calculating...")
                }
            } else {
                list.add("$posLabel: Calculating...")
                list.add("$tillLabel: Calculating...")
                list.add("$etaLabel: Calculating...")
            }
        }

        if (previousPosition) {
            list.add("")
            if (previousUser != null) {
                val wipedSuffix = if (isPreviousWiped) "-wiped" else ""
                list.add("Passed ($previousUser$wipedSuffix): ${NumbersUtils.formatNumber(previousAmount)}")
            } else {
                list.add("Passed: Calculating...")
            }

            if (abovePrevious == -1L) {
                list.add("Difference: Calculating...")
            } else {
                list.add("Difference: ${NumbersUtils.formatNumber(abovePrevious)}")
            }
        }
    }
}