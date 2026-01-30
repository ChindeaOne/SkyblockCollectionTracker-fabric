package io.github.chindeaone.collectiontracker.util.chat

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.tracker.sacks.SacksTrackingManager
import io.github.chindeaone.collectiontracker.util.StringUtils.removeColor
import net.minecraft.network.chat.Component

object ChatListener {

    private val MINING_PATTERN = Regex("""§3\+([0-9]+(?:\.[0-9]+)?)\s+Mining\s*\(\s*([0-9,]+)\s*/\s*([0-9,]+|0)\s*\)""")
    private val SACKS_PATTERN = Regex("""^\[Sacks]\s*\+([0-9,]+)\s+items?\.\s*\(Last\s+([0-9]+)s\.?\)""", RegexOption.IGNORE_CASE)

    fun handle(message: Component): Boolean {
        val text = message.string.removeColor()

        if (text.startsWith("[Sacks]")) {
            somethingSacks(text)
        }
        return true

        // TODO: Handle skill tracking later
//        val match = MINING_PATTERN.find(text)
//        if (match != null) {
//            val gains = match.groupValues[1].toDoubleOrNull()
//            val firstStat = match.groupValues[2].replace(",", "").toLongOrNull()
//            val secondStat = match.groupValues[3].replace(",", "").toLongOrNull()
//        }

    }

    fun somethingSacks(message: String) {
        if (!ConfigAccess.isSacksTrackingEnabled()) return

        val match = SACKS_PATTERN.find(message)
        if (match != null) {
            val itemsStr = match.groupValues[1].replace(",", "") // Remove commas
            val items = itemsStr.toIntOrNull() ?: 0
            val timeframe = match.groupValues[2].toIntOrNull() ?: 0

            SacksTrackingManager.onChatCollection(items, timeframe)
        }
    }
}