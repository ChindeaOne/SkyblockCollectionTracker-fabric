package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import io.github.chindeaone.collectiontracker.utils.world.WaypointsUtils
import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot
import kotlin.math.ceil
import kotlin.math.roundToInt

object ScoreboardUtils {

    private val locationSymbols: Regex = Regex("[\uE067\uE020]\\s*") // ф is for Rift
    private val timeRegex = Regex("(\\d{1,2}):(\\d{2})(am|pm)")
    private val scoreboardTitlePattern = Regex("SK[YI]BLOCK(?: CO-OP| GUEST)?(?: [♲☀Ⓑ])?")

    var location: String = ""
    var lastLocation: String = ""
    var mineshaftType: String = ""

    var heatValue: Int = 0
    var coldValue: Int = 0

    var checkTime: Boolean = true
    var timeLeft: Int = 0
    private var lastMinutesSinceMidnight = -1

    fun onClientTick(client: Minecraft) {
        if (!HypixelUtils.isOnSkyblock) return
        if (ModLoader.clientTicks % 4L != 0L) return

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
                val formatted = strLine.removeColor().trim()

                formatted.ifEmpty { null }
            }

        checkSkyblockTime(rawLines)
        checkLocation(rawLines)
        checkIfMineshaft(rawLines)
        getHeatValue(rawLines)
        getColdValue(rawLines)
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
        val timeLine = rawLines.firstOrNull { it.contains("am") || it.contains("pm") } ?: return

        timeRegex.find(timeLine)?.let { result ->
            val hour = result.groupValues[1].toInt()
            val minute = result.groupValues[2].toInt()
            val amPm = result.groupValues[3]

            val sb10Minutes = 1200.0 / 144.0

            val hour24 = when {
                amPm.equals("am", ignoreCase = true) && hour == 12 -> 0
                amPm.equals("am", ignoreCase = true) -> hour
                amPm.equals("pm", ignoreCase = true) && hour == 12 -> 12
                else -> hour + 12
            }

            val minutesSinceMidnight = hour24 * 60 + minute

            // Initial sync when joining
            if (checkTime) {
                val totalMinutesInDay = 24 * 60
                var minutesUntilMidnight = (totalMinutesInDay - minutesSinceMidnight) % totalMinutesInDay

                if (minutesSinceMidnight == 0) minutesUntilMidnight = 0

                val tenMinuteChunks =
                    if (minutesUntilMidnight == 0) 0.0
                    else ceil(minutesUntilMidnight / 10.0)

                val secondsLeft = tenMinuteChunks * sb10Minutes

                timeLeft = secondsLeft.roundToInt()
                checkTime = false
                ChatListener.nextBuffTime = System.currentTimeMillis() + (timeLeft * 1000L)
            }

            // Detect transition to a new SkyBlock day
            if (minutesSinceMidnight == 0 && lastMinutesSinceMidnight != 0) {
                ChatListener.nextBuffTime = System.currentTimeMillis() + 20 * 60 * 1000L
            }

            lastMinutesSinceMidnight = minutesSinceMidnight
        }
    }

    fun checkScoreboard(client: Minecraft): Boolean? {
        val world = client.level ?: return null
        val objective = world.scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return null
        val displayName = objective.displayName.string

        val scoreboardTitle = displayName.removeColor()
        return scoreboardTitlePattern.matches(scoreboardTitle)
    }

    private fun checkIfMineshaft(rawLines: List<String>) {
        if (!IslandTracker.currentMiningIsland.equals("Mineshaft")) {
            if (mineshaftType.isNotEmpty()) {
                mineshaftType = ""
            }
            return
        }

        val foundType = rawLines.firstNotNullOfOrNull { line ->
            WaypointsUtils.MineshaftTypes.entries.find {type ->
                line.contains(type.name)
            }?.name
        } ?: ""

        if (mineshaftType != foundType) {
            mineshaftType = foundType

            val category = when {
                foundType.endsWith("_C") -> "crystal"
                foundType == "JASP_1" -> "jasper"
                foundType == "TUNG_1" -> "tungsten_shaft"
                else -> null
            }

            if (category != null) {
                WaypointsUtils.selectCategory(category)
            } else {
                WaypointsUtils.currentCategory = null
            }
        }
    }

    private fun getColdValue(rawLines: List<String>) {
        if (!isColdStatRelevant()) return

        val cold = rawLines.firstNotNullOfOrNull { line ->
            val match = Regex("""Cold:\s*(-?\d+)""").find(line)
            match?.groupValues[1]?.toIntOrNull()
        }

        coldValue = cold ?: 0
    }

    private fun getHeatValue(rawLines: List<String>) {
        if (!isHeatStatRelevant()) return

        val heat = rawLines.firstNotNullOfOrNull { line ->
            val match = Regex("""Heat:\s*(-?\d+)""").find(line)
            match?.groupValues[1]?.toIntOrNull()
        }

        heatValue = heat ?: 0
    }

    fun isColdStatRelevant(): Boolean {
        return location == "Glacite Tunnels" || location == "Glacite Mineshafts" || location == "Great Glacite Lake" || location == "Dwarven Base Camp" || location == "Grandpa Wolf's Cave"
    }

    fun isHeatStatRelevant(): Boolean {
        return location == "Magma Fields"
    }
}