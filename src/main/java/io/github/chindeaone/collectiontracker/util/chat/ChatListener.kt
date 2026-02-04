package io.github.chindeaone.collectiontracker.util.chat

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.tracker.sacks.SacksTrackingManager
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.util.HypixelUtils
import io.github.chindeaone.collectiontracker.util.StringUtils.removeColor
import net.minecraft.network.chat.Component

object ChatListener {

    // Skyhanni skill pattern
    private val SKILL_PATTERN = Regex("\\+(?<gains>[\\d,.]+)\\s+(?<skillName>.+?)\\s*\\((?<current>[\\d,]+)\\s*/\\s*(?<needed>[\\d,]+)\\)", RegexOption.IGNORE_CASE)
    private val SACKS_PATTERN = Regex("""^\[Sacks]\s*\+([0-9,]+)\s+items?\.\s*\(Last\s+([0-9]+)s\.?\)""", RegexOption.IGNORE_CASE)

    var lastSkillValue = 0L

    fun handle(message: Component) {
        if (!HypixelUtils.isOnSkyblock) return

        val text = message.string.removeColor()

        if (text.startsWith("[Sacks]")) {
            parseSacksMessage(text)
            return
        }

        // Check pattern for past max skill level
        val match = SKILL_PATTERN.find(text)
        if (match != null) {
            parseSkillMessage(match)
            return
        }
    }

    private fun parseSacksMessage(message: String) {
        if (!ConfigAccess.isSacksTrackingEnabled()) return

        val match = SACKS_PATTERN.find(message)
        if (match != null) {
            val itemsStr = match.groupValues[1].replace(",", "")
            val items = itemsStr.toIntOrNull() ?: 0
            val timeframe = match.groupValues[2].toIntOrNull() ?: 0

            SacksTrackingManager.onChatCollection(items, timeframe)
        }
    }

    private fun parseSkillMessage(match: MatchResult) {
        val currentRaw = match.groups["current"]?.value ?: return
        val skillName = match.groups["skillName"]?.value?.trim() ?: return

        val current = currentRaw.replace(",", "").toLongOrNull() ?: return
        if (current != lastSkillValue) {
            SkillTrackingHandler.onSkillGain(current, skillName)
            lastSkillValue = current
        }
    }
}