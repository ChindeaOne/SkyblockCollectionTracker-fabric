package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.enableTamingTracking
import io.github.chindeaone.collectiontracker.config.leaderboardPosition
import io.github.chindeaone.collectiontracker.config.previousPosition
import io.github.chindeaone.collectiontracker.config.skillLeaderboard
import io.github.chindeaone.collectiontracker.config.skillPosition
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

    override val position: Position get() = skillPosition

    override val isEnabled: Boolean get() = SkillTrackingHandler.isTracking

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled) return
        if (lines.isEmpty()) return

        drawOverlayFrame(context, position) { renderSkillStringsWithTaming(context, cachedSkillLines, cachedTamingLines) }
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

        val currentSkill = SkillTracker.skillName
        val currentSkillRank = SkillTrackingRates.currentSkillRank

        val withTaming = enableTamingTracking && currentSkill != "Taming"
        val currentTamingTotalXp = SkillTrackingRates.tamingXp + SkillTrackingRates.tamingXpGained
        val currentTamingRank = SkillTrackingRates.currentTamingRank

        val leaderboard = skillLeaderboard

        val skillLines = mutableListOf<String>()
        val rankSuffix = formatRankSuffix(currentSkillRank, leaderboard)

        skillLines.add("$currentSkill Level: ${formatNumber(SkillTrackingRates.skillLevel.toLong())}$rankSuffix")
        skillLines.add("Total $currentSkill XP: ${formatNumberOrPlaceholder(SkillTrackingRates.totalSkillXp)}")
        skillLines.add("XP (Session): ${formatNumberOrPlaceholder(SkillTrackingRates.skillXpGained)}")
        skillLines.add("XP/h: ${formatNumberOrPlaceholder(SkillTrackingRates.skillPerHour)}")

        addLeaderboardLines(
            skillLines,
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
        skillLines.add("Uptime: ${SkillTrackingHandler.uptime}")

        val tamingLines = mutableListOf<String>()
        if (withTaming) {
            val tamingRankSuffix = formatRankSuffix(currentTamingRank, leaderboard)

            tamingLines.add("Taming Level: ${formatNumber(SkillTrackingRates.tamingLevel.toLong())}$tamingRankSuffix")
            tamingLines.add("Total Taming XP: ${formatNumberOrPlaceholder(currentTamingTotalXp)}")
            tamingLines.add("XP (Session): ${formatNumberOrPlaceholder(SkillTrackingRates.tamingXpGained)}")
            tamingLines.add("XP/h: ${formatNumberOrPlaceholder(SkillTrackingRates.tamingPerHour)}")

            addLeaderboardLines(
                tamingLines,
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

        cachedSkillLines = skillLines
        cachedTamingLines = tamingLines

        cachedLines = buildList {
            addAll(skillLines)
            if (withTaming && tamingLines.isNotEmpty()) {
                add("")
                addAll(tamingLines)
            }
        }
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
                list.add("$posLabel ($nextUser$wipedSuffix): ${formatNumber(nextAmount)}")
                if (tillNext == -1L) {
                    list.add("$tillLabel: Calculating...")
                } else {
                    list.add("$tillLabel: ${formatNumber(tillNext)}")
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
                list.add("Passed ($previousUser$wipedSuffix): ${formatNumber(previousAmount)}")
            } else {
                list.add("Passed: Calculating...")
            }

            if (abovePrevious == -1L) {
                list.add("Difference: Calculating...")
            } else {
                list.add("Difference: ${formatNumber(abovePrevious)}")
            }
        }
    }
}
