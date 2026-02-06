package io.github.chindeaone.collectiontracker.util.chat

import io.github.chindeaone.collectiontracker.coleweight.ColeweightManager
import io.github.chindeaone.collectiontracker.coleweight.ColeweightUtils
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.tracker.sacks.SacksTrackingManager
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.util.HypixelUtils
import io.github.chindeaone.collectiontracker.util.ScoreboardUtils
import io.github.chindeaone.collectiontracker.util.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.util.world.MiningMapping
import net.minecraft.network.chat.Component

object ChatListener {

    // Skyhanni skill pattern
    private val SKILL_PATTERN = Regex("\\+(?<gains>[\\d,.]+)\\s+(?<skillName>.+?)\\s*\\((?<current>[\\d,]+)\\s*/\\s*(?<needed>[\\d,]+)\\)", RegexOption.IGNORE_CASE)
    private val SACKS_PATTERN = Regex("""^\[Sacks]\s*\+([0-9,]+)\s+items?\.\s*\(Last\s+([0-9]+)s\.?\)""", RegexOption.IGNORE_CASE)
    // Coleweight pattern
    private val NAME_PATTERN = Regex( """^(?:\[\d+]\s+)?(?:⸕\s+)?(?:(?:Guild|Party|Co-op)\s*>\s+|\[:v:]\s+)?(?:\[[^]]+]\s+)?([A-Za-z0-9_]{1,16})(?:\s+♲)?(?:\s+\[[^]]{1,6}])?\s*:\s*(.*)$""", RegexOption.IGNORE_CASE)

    var lastSkillValue = 0L

    fun trackingHandle(message: Component) {
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

    fun coleweightHandle(message: Component): Component {
        if (!HypixelUtils.isOnSkyblock) return message
        if (!ConfigAccess.isColeweightRankingInChat()) return message

        val text = message.string.removeColor()

        if (ConfigAccess.isOnlyOnMiningIslands()) {
            if (!MiningMapping.miningIslands.contains(ScoreboardUtils.location)) return message
        }
        val match = NAME_PATTERN.find(text)?: return message
        val playerName = match.groupValues[1]

        val storage = ColeweightManager.storage
        val leaderboardRank = storage.leaderboard.indexOfFirst { it.name.equals(playerName, ignoreCase = true) }

        if (leaderboardRank != -1) {
            val rank = leaderboardRank + 1
            val rankSuffix = ColeweightUtils.getRankColors(rank)

            if (rankSuffix.isNotEmpty()) {
                val newComponent = Component.empty()
                var addedRank = false

                for (sibling in message.siblings) {
                    val siblingText = sibling.string

                    if (!addedRank && siblingText.contains(playerName)) {
                        newComponent.append(sibling)
                        newComponent.append(Component.literal(" $rankSuffix"))
                        addedRank = true
                    } else {
                        newComponent.append(sibling)
                    }
                }
                return if (addedRank) newComponent else message
            }
        }
        return message
    }

    private fun parseSacksMessage(message: String) {
        if (!ConfigAccess.isSacksTrackingEnabled()) return

        val match = SACKS_PATTERN.find(message)
        if (match != null) {
            val itemsStr = match.groupValues[1].replace(",", "")
            val items = itemsStr.toIntOrNull() ?: 0

            SacksTrackingManager.onChatCollection(items)
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