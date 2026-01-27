package io.github.chindeaone.collectiontracker.util

import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot

object ScoreboardUtils {

    val locationSymbols: Regex = Regex("^[⏣ф]\\s*") // ф is for Rift

    var location: String = ""
    var lastLocation: String = ""

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

        val rawLines = ArrayList<String>()

        for (scoreHolder in scoreboard.trackedPlayers) {
            if (!scoreboard.listPlayerScores(scoreHolder).containsKey(objective)) continue

            val team = scoreboard.getPlayersTeam(scoreHolder.scoreboardName)
            if (team != null) {
                val prefix = team.playerPrefix.string
                val suffix = team.playerSuffix.string
                val strLine = prefix + suffix
                if (strLine.trim().isNotEmpty()) {
                    val formatted = strLine.replace(Regex("§."), "")
                    rawLines.add(formatted)
                }
            }
        }

        val locationLine = rawLines.firstNotNullOfOrNull { line ->
            val s = line.trimStart()
            if (locationSymbols.containsMatchIn(s)) s else null
        }

        if (locationLine != null) {
            val newLocation = locationLine.replace(locationSymbols, "").trim()
            if (lastLocation == newLocation) return
            lastLocation = location
            location = newLocation
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
        return location == "Glacite Tunnels" || location == "Glacite Mineshafts"
    }

    @JvmStatic
    fun isHeatStatRelevant(): Boolean {
        return location == "Magma Fields"
    }
}