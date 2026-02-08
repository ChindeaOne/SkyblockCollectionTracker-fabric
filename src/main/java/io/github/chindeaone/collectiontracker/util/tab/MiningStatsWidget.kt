package io.github.chindeaone.collectiontracker.util.tab

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.util.ChatUtils
import io.github.chindeaone.collectiontracker.util.world.MiningMapping

object MiningStatsWidget {

    private var lastStats: List<String>? = null
    var rawStats: List<String> = emptyList()
    var currentMiningIsland: String? = null
        private set

    private var nextAllowedTime: Long = 0L
    private var firstInfoSeenTime: Long = 0L

    fun onTabWidgetsUpdate() {
        if (!ConfigAccess.isMiningStatsEnabled()) {
            rawStats = emptyList()
            currentMiningIsland = null
            lastStats = null
            return
        }
        // Check if the player is in a mining area
        val areaWidget = TabWidget.AREA

        if (areaWidget.isPresent) {
            currentMiningIsland = if (ConfigAccess.isMiningStatsOverlayInMiningIslandsOnly()) {
                areaWidget.lines.firstNotNullOfOrNull { line ->
                    MiningMapping.miningIslands.firstOrNull { name ->
                        line.contains(name, ignoreCase = true)
                    }
                }
            } else areaWidget.lines.firstNotNullOfOrNull { line ->
                MiningMapping.miningAreas.firstOrNull { name ->
                    line.contains(name, ignoreCase = true)
                }
            }

            if (currentMiningIsland == null) {
                rawStats = emptyList()
                return
            }
        } else {
            rawStats = emptyList()
            return
        }

        val widget = TabWidget.STATS
        val now = System.currentTimeMillis()

        if (!widget.isPresent) {
            // avoid spamming messages when tab widgets are not visible
            if (!TabWidget.INFO.isPresent) {
                firstInfoSeenTime = 0L
                return
            }

            if (firstInfoSeenTime == 0L) {
                firstInfoSeenTime = now
            }

            if (now - firstInfoSeenTime < 5_000L) {
                return // Wait for the 5s buffer
            }

            // disable the overlay if the widget is not found
            ChatUtils.sendMessage("§cWarning: Stats widget not found. This can happen in low TPS lobbies. Please enable it using /widget or re-enable the stats overlay config in your mod.", true)
            ConfigHelper.disableMiningStats()
            return
        }

        // Check if the widget contains any mining stats
        val hasMiningStats = widget.lines.any { line ->
            MiningMapping.miningStats.any { stat -> line.contains(stat, ignoreCase = true)}
        }

        if (!hasMiningStats) {
            rawStats = emptyList()
            return
        }

        firstInfoSeenTime = 0L

        if (now < nextAllowedTime) return

        val currentRaw = TabData.parseWidgetData(widget.lines)
        if (currentRaw == null || currentRaw == lastStats) return

        rawStats = currentRaw
        lastStats = currentRaw
        nextAllowedTime = now + 3_000L // same as Hypixel

        rawStats.joinToString(" | ") { it }
    }
}