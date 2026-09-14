package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.categories.milestones.Milestone
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingRates
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.NumbersUtils
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.utils.SoundUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

class SkillMilestoneOverlay: AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    private var initialized = false

    override val overlayLabel: String = "Skill Milestones Overlay"

    override val position: Position get() = ConfigAccess.getSkillMilestonesPosition()

    override val isEnabled: Boolean get() = SkillTrackingHandler.isTracking && ConfigAccess.isSkillMilestonesEnabled()

    private fun notificationTitle(name: String): Component = Component.literal("§6[§3§kd§6]")
        .append(Component.literal(" $name Milestone").withColor(ColorUtils.skillColors[name] ?: Colors.GREEN.color))
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

        RenderUtils.drawOverlayFrame(context, position) { RenderUtils.renderMilestoneStrings(context, lines, isCollection = false) }
    }

    private fun updateLinesIfNeeded() {
        if (!isEnabled) {
            cachedLines = emptyList()
            notifiedMilestones.clear()
            initialized = false
            return
        }

        if (ModLoader.clientTicks % 5L != 0L) return

        val skillName = SkillTracker.skillName
        val formatSkillName = skillName.lowercase()
        val milestone = ConfigAccess.getMilestones()[formatSkillName]

        if (milestone == null || !SkillUtils.isValidSkill(skillName)) {
            cachedLines = emptyList()
            return
        }

        if (!initialized) {
            initializeCompletedMilestone(skillName, milestone)
            initialized = true
        }

        cachedLines = listOf(
            formatMilestone(skillName, milestone, getCurrentAmount(milestone))
        )
    }

    private fun initializeCompletedMilestone(name: String, milestone: Milestone) {
        if (getCurrentAmount(milestone) >= milestone.target) {
            notifiedSkillMilestones.add(name)
        }
    }

    private fun getCurrentAmount(milestone: Milestone): Long {
        return when {
            milestone.isTotal -> SkillTrackingRates.totalSkillXp
            else -> milestone.progress + SkillTrackingRates.skillXpGained
        }
    }

    private fun formatMilestone(name: String, milestone: Milestone, currentAmount: Long): String {
        val formattedCurrentAmount = NumbersUtils.formatNumber(currentAmount)
        val formattedTargetAmount = NumbersUtils.formatNumber(milestone.target)

        if (currentAmount < milestone.target) {
            return "$name: $formattedCurrentAmount / $formattedTargetAmount"
        }

        notifyMilestoneCompletion(name)

        return "$name: §aCompleted"
    }

    private fun notifyMilestoneCompletion(name: String) {
        if (!notifiedSkillMilestones.add(name)) return

        handleTitleNotification(name)
        handleSoundNotification()
        handleChatNotification(name)
    }

    private fun handleTitleNotification(name: String) {
        if (ConfigAccess.isSkillMilestonesTitleNotificationEnabled()) {
            RenderUtils.showTitle(notificationTitle(name))
        }
    }

    private fun handleSoundNotification() {
        if (ConfigAccess.isSkillMilestonesSoundNotificationEnabled()) {
            SoundUtils.playSound()
        }
    }

    private fun handleChatNotification(name: String) {
        val message = Component.empty()
            .append(Component.literal("$name Milestone").withColor(ColorUtils.skillColors[name] ?: Colors.GREEN.color))
            .append(Component.literal(" completed!").withColor(Colors.GREEN.color))
            .append(Component.literal(" Click here to remove this milestone.").withColor(Colors.YELLOW.color))

        ChatUtils.sendHoverableCommandComponent(
            message,
            "§cClick to remove this milestone",
            "/sct milestones remove $name"
        )
    }
}
val notifiedSkillMilestones = mutableSetOf<String>()

fun clearNotifiedSkillMilestone(name: String) {
    notifiedSkillMilestones.remove(name)
}

fun saveSkillMilestoneProgress() {
    val skillName = SkillTracker.skillName.lowercase()
    val milestone = ConfigAccess.getMilestones()[skillName] ?: return

    if (milestone.isTotal) return

    milestone.progress += SkillTrackingRates.skillXpGained
    ConfigHelper.saveMilestones(ConfigAccess.getMilestones())
}