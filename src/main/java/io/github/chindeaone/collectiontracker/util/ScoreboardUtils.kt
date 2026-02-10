package io.github.chindeaone.collectiontracker.util

import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot
import kotlin.math.ceil
import kotlin.math.roundToInt

object ScoreboardUtils {

    private val locationSymbols: Regex = Regex("^[⏣ф]\\s*") // ф is for Rift
    private val timeRegex = Regex("(\\d{1,2}):(\\d{2})(am|pm)")

    var location: String = ""
    var lastLocation: String = ""

    private var checkTime: Boolean = true
    var timeLeft: Int = 0

    fun onTick(client: Minecraft) {
        if (!HypixelUtils.isOnSkyblock) return

        val world = client.level ?: return
        val scoreboard = world.scoreboard
        val objective = scoreboard.getDisplayObjective(DisplaySlot.BY_ID.apply(1))

        if (objective == null) {
            location = ""
            lastLocation = ""
            return
        }

        val rawLines = scoreboard.listPlayerScores(objective)
            .sortedByDescending { it.value }
            .mapNotNull { score ->
                val team = scoreboard.getPlayersTeam(score.ownerName().string) ?: return@mapNotNull null
                val prefix = team.playerPrefix.string
                val suffix = team.playerSuffix.string
                val strLine = prefix + suffix
                val formatted = strLine.replace(Regex("§."), "").trim()

                formatted.ifEmpty { null }
            }

        checkSkyblockTime(rawLines)
        checkLocation(rawLines)
    }

    private fun checkLocation(rawLines: List<String>) {
        val locationLine = rawLines.firstNotNullOfOrNull { line ->
            val s = line.trimStart()
            if (locationSymbols.containsMatchIn(s)) s else null
        }

        if (locationLine != null) {
            val newLocation = locationLine.replace(locationSymbols, "").trim()
            if (location != newLocation) {
                lastLocation = location
                location = newLocation
            }
        }
    }

    private fun checkSkyblockTime(rawLines: List<String>) {
        if (!checkTime) return

        val timeLine = rawLines.firstOrNull { it.contains("am") || it.contains("pm") } ?: return

        timeRegex.find(timeLine)?.let { result ->
            val hour = result.groupValues[1].toInt()
            val minute = result.groupValues[2].toInt()
            val amPm = result.groupValues[3]

            // Skyblock time mapping
            val sb10Minutes = 8.3 // real seconds per in-game 10 minutes

            // Convert parsed 12-hour time to 24-hour hour value
            val hour24 = when {
                amPm.equals("am", ignoreCase = true) && hour == 12 -> 0
                amPm.equals("am", ignoreCase = true) -> hour
                amPm.equals("pm", ignoreCase = true) && hour == 12 -> 12
                else -> hour + 12
            }

            val minutesSinceMidnight = hour24 * 60 + minute
            val totalMinutesInDay = 24 * 60
            var minutesUntilMidnight = (totalMinutesInDay - minutesSinceMidnight) % totalMinutesInDay

            if (minutesSinceMidnight == 0) minutesUntilMidnight = 0

            // Skyblock time only updates in 10-minute increments
            val tenMinuteChunks = if (minutesUntilMidnight == 0) 0.0 else ceil(minutesUntilMidnight / 10.0)
            val secondsLeft = tenMinuteChunks * sb10Minutes

            timeLeft = secondsLeft.roundToInt()
            checkTime = true
        }
    }

    fun getScoreboardTitle(client: Minecraft): String? {
        val world = client.level ?: return null
        val objective = world.scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return null
        val displayName = objective.displayName?.string ?: return null
        return displayName
    }

    @JvmStatic
    fun isColdStatRelevant(): Boolean {
        return location == "Glacite Tunnels" || location == "Glacite Mineshafts" || location == "Great Glacite Lake"
    }

    @JvmStatic
    fun isHeatStatRelevant(): Boolean {
        return location == "Magma Fields"
    }
}