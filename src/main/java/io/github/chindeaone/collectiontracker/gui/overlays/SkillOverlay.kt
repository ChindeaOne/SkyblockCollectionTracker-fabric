package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getSkillPosition
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
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
                cachedSkillLines = emptyList()
                cachedTamingLines = emptyList()
            }
            return
        }

        if (ModLoader.clientTicks % 5L != 0L) return

        val currentUptime = SkillTrackingHandler.uptime
        val currentSkill = SkillTracker.skillName
        val currentSkillLvl = SkillTrackingRates.skillLevel
        val currentTotalXp = SkillTrackingRates.totalSkillXp
        val currentSkillGained = SkillTrackingRates.skillXpGained
        val currentSkillPerHour = SkillTrackingRates.skillPerHour
        val currentSkillRank = SkillTrackingRates.skillCurrentRank
        val currentSkillNextUser = SkillTrackingRates.skillNextRankUsername
        val currentSkillNextAmount = SkillTrackingRates.skillNextRankAmount
        val currentSkillTillNext = SkillTrackingRates.skillTillNextRank
        val currentSkillEta = SkillTrackingRates.skillEtaToNextRank

        val withTaming = isTamingTrackingEnabled() && currentSkill != "Taming"
        val currentTamingLvl = SkillTrackingRates.tamingLevel
        val currentTamingTotalXp = SkillTrackingRates.tamingXp + SkillTrackingRates.tamingXpGained
        val currentTamingGained = SkillTrackingRates.tamingXpGained
        val currentTamingPerHour = SkillTrackingRates.tamingPerHour
        val currentTamingRank = SkillTrackingRates.tamingCurrentRank
        val currentTamingNextUser = SkillTrackingRates.tamingNextRankUsername
        val currentTamingNextAmount = SkillTrackingRates.tamingNextRankAmount
        val currentTamingTillNext = SkillTrackingRates.tamingTillNextRank
        val currentTamingEta = SkillTrackingRates.tamingEtaToNextRank
        val leaderboard = isSkillLeaderboardEnabled()

        val newSkillLines = mutableListOf<String>()
        var rankSuffix = ""
        if (leaderboard && currentSkillRank != -1) {
            rankSuffix = if (currentSkillRank == 10001) {
                " [Too low]"
            } else " [#$currentSkillRank]"
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
            leaderboard
        )
        newSkillLines.add("Uptime: $currentUptime")

        val newTamingLines = mutableListOf<String>()
        if (currentSkill == "Taming") {
            disableTamingTracking()
        } else if (withTaming) {
            var tamingRankSuffix = ""
            if (leaderboard && currentTamingRank != -1) {
                tamingRankSuffix = if (currentTamingRank == 10001) {
                    " [Too low]"
                } else " [#$currentTamingRank]"
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
        leaderboardEnabled: Boolean
    ) {
        if (!leaderboardEnabled) return
        if (rank == 1) return

        list.add("")

        if (nextUser != null) {
            list.add(String.format("Next Position (%s): %s", nextUser, formatNumber(nextAmount)))
            list.add("Till Next Position: " + formatNumber(tillNext))
            if (!eta.isNullOrEmpty()) {
                list.add("ETA: $eta")
            } else {
                list.add("ETA: Calculating...")
            }
        } else {
            list.add("Next Position: Calculating...")
            list.add("Till Next Position: Calculating...")
            list.add("ETA: Calculating...")
        }
    }
}
