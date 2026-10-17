package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.fromNow
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.SkyblockTimeUtils.toRealSeconds
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import io.github.chindeaone.collectiontracker.utils.world.WaypointsUtils
import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object ScoreboardUtils {

    private val locationSymbols: Regex = Regex("[\uE067\uE020]\\s*") // ф is for Rift
    private val timeRegex = Regex("(\\d{1,2}):(\\d{2})(am|pm)")
    private val scoreboardTitlePattern = Regex("SK[YI]BLOCK(?: CO-OP| GUEST)?(?: [♲☀Ⓑ])?")

    var location: String = ""
    var lastLocation: String = ""
    var mineshaftType: String = ""

    var heatValue: Int = 0
    var coldValue: Int = 0

    var nextBuffTime: EpochMark? = null
    var updated: Boolean = false
    var timeLeft: Duration = Duration.ZERO

    var checkTime: Boolean = true

    fun onClientTick(client: Minecraft) {
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
        checkMineshaftType(rawLines)
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
            val period = result.groupValues[3]

            val untilMidnight = timeUntilMidnight(hour, minute, period)

            // Initial sync when joining
            if (checkTime) {
                timeLeft = untilMidnight
                nextBuffTime = untilMidnight.fromNow()
                checkTime = false
            }

            // Detect transition to a new SkyBlock day
            val isMidnight = hour == 12 && minute == 0 && period == "am"

            if (isMidnight) {
                if (!updated) {
                    nextBuffTime = 20.minutes.fromNow()
                    updated = true
                } else {
                    updated = false
                }
            }
        }
    }

    private fun timeUntilMidnight(hour: Int, minute: Int, period: String): Duration {
        val minutesSinceMidnight = skyblockMinutesSinceMidnight(hour, minute, period)
        val minutesUntilMidnight = (24 * 60 - minutesSinceMidnight) % (24 * 60)
        return minutesUntilMidnight.toRealSeconds()
    }

    private fun skyblockMinutesSinceMidnight(hour: Int, minute: Int, period: String): Int {
        val normalizedHour = when (period) {
            "am" if hour == 12 -> 0
            "am" -> hour
            "pm" if hour == 12 -> 12
            else -> hour + 12
        }

        return SkyblockTimeUtils.skyblockTimeInMinutes(normalizedHour, minute)
    }

    fun checkScoreboard(client: Minecraft): Boolean? {
        val world = client.level ?: return null
        val objective = world.scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return null
        val displayName = objective.displayName.string

        val scoreboardTitle = displayName.removeColor()
        return scoreboardTitlePattern.matches(scoreboardTitle)
    }

    private fun checkMineshaftType(rawLines: List<String>) {
        if (!IslandTracker.currentMiningIsland.equals("Mineshaft")) {
            if (mineshaftType.isNotEmpty()) {
                mineshaftType = ""
            }
            return
        }

        val foundType = rawLines.firstNotNullOfOrNull { line ->
            WaypointsUtils.MineshaftTypes.entries.find { type ->
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

    fun isColdStatRelevant(): Boolean = location == "Glacite Tunnels" || location == "Glacite Mineshafts" || location == "Great Glacite Lake" || location == "Dwarven Base Camp" || location == "Grandpa Wolf's Cave"

    fun isHeatStatRelevant(): Boolean = location == "Magma Fields"
}