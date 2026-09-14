package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getSkillPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isLeaderboardPositionEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isPreviousPositionEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isSkillLeaderboardEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isTamingTrackingEnabled
import io.github.chindeaone.collectiontracker.config.ConfigHelper.disableTamingTracking
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingRates
import io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatNumber
import io.github.chindeaone.collectiontracker.utils.StringUtils.formatNumberOrPlaceholder
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.drawOverlayFrame
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.renderSkillStringsWithTaming
import net.minecraft.client.gui.GuiGraphicsExtractor

class SkillOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private var cachedSkillLines: List<String> = emptyList()
    private var cachedTamingLines: List<String> = emptyList()

    override val overlayLabel: String = "Skill Tracker"

    override val position: Position get() = getSkillPosition()

    override val isEnabled: Boolean get() = SkillTrackingHandler.isTracking

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled) return

        updateLinesIfNeeded()
        if (cachedSkillLines.isEmpty()) return

        drawOverlayFrame(context, position) {
            renderSkillStringsWithTaming(
                context,
                cachedSkillLines,
                cachedTamingLines,
                isTamingTrackingEnabled() && SkillTracker.skillName != "Taming"
            )
        }
    }

    override fun updateDimensions() {
        if (!isEnabled) return
        updateLinesIfNeeded()

        super.updateDimensions()
    }

    override val lines: List<String>
        get() {
            updateLinesIfNeeded()
            return cachedLines
        }

    private fun updateLinesIfNeeded() {
        if (!isEnabled) {
            cachedLines = emptyList()
            cachedSkillLines = emptyList()
            cachedTamingLines = emptyList()
            return
        }

        if (ModLoader.clientTicks % 5L != 0L) return

        val currentUptime = SkillTrackingHandler.uptime
        val currentSkill = SkillTracker.skillName
        val currentSkillLvl = SkillTrackingRates.skillLevel
        val currentTotalXp = SkillTrackingRates.totalSkillXp
        val currentSkillGained = SkillTrackingRates.skillXpGained
        val currentSkillPerHour = SkillTrackingRates.skillPerHour
        val currentSkillRank = SkillTrackingRates.currentSkillRank
        val currentSkillNextUser = SkillTrackingRates.nextSkillRankUsername
        val currentSkillNextAmount = SkillTrackingRates.nextSkillRankAmount
        val currentSkillTillNext = SkillTrackingRates.tillNextSkillRank
        val currentSkillEta = SkillTrackingRates.etaToNextSkillRank
        val isNextSkillWiped = SkillTrackingRates.isNextSkillWiped
        val previousSkillRankUsername = SkillTrackingRates.previousSkillRankUsername
        val previousSkillRankAmount = SkillTrackingRates.previousSkillRankAmount
        val abovePreviousSkillRankAmount = SkillTrackingRates.abovePreviousSkillRankAmount
        val isPreviousSkillWiped = SkillTrackingRates.isPreviousSkillWiped

        val withTaming = isTamingTrackingEnabled() && currentSkill != "Taming"
        val currentTamingLvl = SkillTrackingRates.tamingLevel
        val currentTamingTotalXp = SkillTrackingRates.tamingXp + SkillTrackingRates.tamingXpGained
        val currentTamingGained = SkillTrackingRates.tamingXpGained
        val currentTamingPerHour = SkillTrackingRates.tamingPerHour
        val currentTamingRank = SkillTrackingRates.currentTamingRank
        val currentTamingNextUser = SkillTrackingRates.nextTamingRankUsername
        val currentTamingNextAmount = SkillTrackingRates.nextTamingRankAmount
        val currentTamingTillNext = SkillTrackingRates.tillNextTamingRank
        val currentTamingEta = SkillTrackingRates.etaToNextTamingRank
        val isNextTamingWiped = SkillTrackingRates.isNextTamingWiped
        val previousTamingRankUsername = SkillTrackingRates.previousTamingRankUsername
        val previousTamingRankAmount = SkillTrackingRates.previousTamingRankAmount
        val abovePreviousTamingRankAmount = SkillTrackingRates.abovePreviousTamingRankAmount
        val isPreviousTamingWiped = SkillTrackingRates.isPreviousTamingWiped

        val leaderboard = isSkillLeaderboardEnabled()

        val newSkillLines = mutableListOf<String>()
        var rankSuffix = ""
        if (leaderboard && currentSkillRank != -1) {
            rankSuffix = if (currentSkillRank == 10001) " [Too low]" else " [#$currentSkillRank]"
        }
        newSkillLines.add("$currentSkill Level: " + formatNumber(currentSkillLvl.toLong()) + rankSuffix)
        newSkillLines.add("Total $currentSkill XP: " + formatNumberOrPlaceholder(currentTotalXp))
        newSkillLines.add("XP (Session): " + formatNumberOrPlaceholder(currentSkillGained))
        newSkillLines.add("XP/h: " + formatNumberOrPlaceholder(currentSkillPerHour))

        addLeaderboardLines(
            newSkillLines,
            currentSkillRank,
            currentSkillNextUser,
            currentSkillNextAmount,
            currentSkillTillNext,
            currentSkillEta,
            isNextSkillWiped,
            previousSkillRankUsername,
            previousSkillRankAmount,
            abovePreviousSkillRankAmount,
            isPreviousSkillWiped,
            leaderboard
        )
        newSkillLines.add("Uptime: $currentUptime")

        val newTamingLines = mutableListOf<String>()
        if (currentSkill == "Taming") {
            disableTamingTracking()
        } else if (withTaming) {
            var tamingRankSuffix = ""
            if (leaderboard && currentTamingRank != -1) {
                tamingRankSuffix = if (currentTamingRank == 10001) " [Too low]" else " [#$currentTamingRank]"
            }
            newTamingLines.add("Taming Level: " + formatNumber(currentTamingLvl.toLong()) + tamingRankSuffix)
            newTamingLines.add("Total Taming XP: " + formatNumberOrPlaceholder(currentTamingTotalXp))
            newTamingLines.add("XP (Session): " + formatNumberOrPlaceholder(currentTamingGained))
            newTamingLines.add("XP/h: " + formatNumberOrPlaceholder(currentTamingPerHour))

            addLeaderboardLines(
                newTamingLines,
                currentTamingRank,
                currentTamingNextUser,
                currentTamingNextAmount,
                currentTamingTillNext,
                currentTamingEta,
                isNextTamingWiped,
                previousTamingRankUsername,
                previousTamingRankAmount,
                abovePreviousTamingRankAmount,
                isPreviousTamingWiped,
                leaderboard
            )
        }

        cachedSkillLines = newSkillLines
        cachedTamingLines = newTamingLines

        val combined = mutableListOf<String>()
        combined.addAll(newSkillLines)
        if (withTaming && newTamingLines.isNotEmpty()) {
            combined.add("")
            combined.addAll(newTamingLines)
        }
        cachedLines = combined
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
        if (rank == 1) return

        val customPos = isLeaderboardPositionEnabled()
        val posLabel = if (customPos) "Custom Position" else "Next Position"
        val tillLabel = if (customPos) "Till Custom Position" else "Till Next Position"
        val etaLabel = if (customPos) "ETA to Custom Position" else "ETA"

        list.add("")

        if (nextUser != null) {
            val wipedSuffix = if (isNextWiped) "-wiped" else ""
            list.add("$posLabel ($nextUser$wipedSuffix): ${formatNumber(nextAmount)}")
            if (tillNext == -1L) {
                list.add("$tillLabel: Calculating...")
            } else {
                list.add("$tillLabel: " + formatNumber(tillNext))
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

        if (isPreviousPositionEnabled()) {
            list.add("")
            if (previousUser != null) {
                val wipedSuffix = if (isPreviousWiped) "-wiped" else ""
                list.add("Passed ($previousUser$wipedSuffix): ${formatNumber(previousAmount)}")
            } else {
                list.add("Passed: Calculating...")
            }

            if (abovePrevious == -1L) {
                list.add("Difference: Calculating...")
            } else {
                list.add("Difference: " + formatNumber(abovePrevious))
            }
        }
    }
}
