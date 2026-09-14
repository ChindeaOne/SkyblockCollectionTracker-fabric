package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.categories.milestones.Milestone
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingRates
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingRates
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.NumbersUtils
import io.github.chindeaone.collectiontracker.utils.SoundUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

class CollectionMilestoneOverlay: AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    private var initialized = false

    override val overlayLabel: String = "Collection Milestones Overlay"

    override val position: Position get() = ConfigAccess.getCollectionMilestonesPosition()

    override val isEnabled: Boolean get() = (TrackingHandler.isTracking || MultiTrackingHandler.isMultiTracking) && ConfigAccess.isCollectionMilestonesEnabled()

    private fun notificationTitle(name: String): Component = Component.literal("§6[§3§kd§6]")
        .append(Component.literal(" ${StringUtils.formatCollectionName(name)} Milestone").withColor(ColorUtils.collectionColors[name] ?: Colors.GREEN.color))
        .append(Component.literal(" Completed!").withColor(Colors.GREEN.color))
        .append(Component.literal(" §6[§3§kd§6]"))

    override val lines: List<String>
        get() {
            updateLinesIfNeeded()
            return cachedLines
        }

    override fun render(context: GuiGraphicsExtractor) {
        if (!isEnabled) return

        val lines = lines
        if (lines.isEmpty()) return

        RenderUtils.drawOverlayFrame(context, position) { RenderUtils.renderMilestoneStrings(context, lines) }
    }

    private fun updateLinesIfNeeded() {
        if (!isEnabled) {
            cachedLines = emptyList()
            notifiedMilestones.clear()
            initialized = false
            return
        }

        if (ModLoader.clientTicks % 5L != 0L) return

        val milestones = ConfigAccess.getMilestones().filterKeys { CollectionsManager.isValidCollection(it) }

        if (!initialized) {
            initializeCompletedMilestones(milestones)
            initialized = true
        }

        cachedLines = milestones.map { (name, milestone) ->
            when {
                TrackingHandler.isTracking -> handleSoloTracking(name, milestone)
                MultiTrackingHandler.isMultiTracking -> handleMultiTracking(name, milestone)
                else -> ""
            }
        }
    }

    private fun initializeCompletedMilestones(milestones: Map<String, Milestone>) {
        milestones.forEach { (name, milestone) ->
            val currentAmount = getCurrentAmount(name, milestone)

            if (currentAmount >= milestone.target) {
                notifiedMilestones.add(name)
            }
        }
    }

    private fun getCurrentAmount(name: String, milestone: Milestone): Long {
        return when {
            TrackingHandler.isTracking && milestone.isTotal -> TrackingRates.collectionAmount
            TrackingHandler.isTracking -> milestone.progress + TrackingRates.collectionMade

            MultiTrackingHandler.isMultiTracking && milestone.isTotal -> MultiTrackingRates.collectionAmounts[name] ?: 0L
            MultiTrackingHandler.isMultiTracking -> milestone.progress + (MultiTrackingRates.collectionMade[name] ?: 0L)

            else -> milestone.progress
        }
    }

    private fun handleSoloTracking(name: String, milestone: Milestone): String {
        return formatMilestone(name, milestone, getCurrentAmount(name, milestone))
    }

    private fun handleMultiTracking(name: String, milestone: Milestone): String {
        return formatMilestone(name, milestone, getCurrentAmount(name, milestone))
    }

    private fun formatMilestone(name: String, milestone: Milestone, currentAmount: Long): String {
        val formattedName = StringUtils.formatCollectionName(name)
        val formattedCurrentAmount = NumbersUtils.formatNumber(currentAmount)
        val formattedTargetAmount = NumbersUtils.formatNumber(milestone.target)

        if (currentAmount < milestone.target) {
            return "$formattedName: $formattedCurrentAmount / $formattedTargetAmount"
        }

        notifyMilestoneCompletion(name)

        return "$formattedName: §aCompleted"
    }

    private fun notifyMilestoneCompletion(name: String) {
        if (!notifiedMilestones.add(name)) return

        handleTitleNotification(name)
        handleSoundNotification()
        handleChatNotification(name)
    }

    private fun handleTitleNotification(name: String) {
        if (ConfigAccess.isCollectionMilestonesTitleNotificationEnabled()) {
            RenderUtils.showTitle(notificationTitle(name))
        }
    }

    private fun handleSoundNotification() {
        if (ConfigAccess.isCollectionMilestonesSoundNotificationEnabled()) {
            SoundUtils.playSound()
        }
    }

    private fun handleChatNotification(name: String) {
        val message = Component.empty()
            .append(Component.literal("${StringUtils.formatCollectionName(name)} Milestone").withColor(ColorUtils.collectionColors[name] ?: Colors.GREEN.color))
            .append(Component.literal(" completed!").withColor(Colors.GREEN.color))
            .append(Component.literal(" Click here to remove this milestone.").withColor(Colors.YELLOW.color))

        ChatUtils.sendHoverableCommandComponent(
            message,
            "§cClick to remove this milestone",
            "/sct milestones remove $name"
        )
    }
}
val notifiedMilestones = mutableSetOf<String>()

fun clearNotifiedMilestone(name: String) {
    notifiedMilestones.remove(name)
}

fun saveMilestonesProgress(isSoloTracker: Boolean) {
    val milestones = ConfigAccess.getMilestones()

    milestones.forEach { (name, milestone) ->
        if (!milestone.isTotal) {
            milestone.progress = when (isSoloTracker) {
                true -> milestone.progress + TrackingRates.collectionMade
                false -> milestone.progress + (MultiTrackingRates.collectionMade[name] ?: 0L)
            }
        }
    }

    ConfigHelper.saveMilestones(milestones)
}