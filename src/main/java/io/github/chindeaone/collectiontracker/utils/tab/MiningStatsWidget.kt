package io.github.chindeaone.collectiontracker.utils.tab

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.ChatUtils
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping

object MiningStatsWidget {

    private var lastStats: List<String>? = null
    var rawStats: List<String> = emptyList()
    @JvmStatic
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
        val now = System.currentTimeMillis()
        if (now < nextAllowedTime) return

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
                lastStats = null
                return
            }
        } else {
            rawStats = emptyList()
            lastStats = null
            return
        }

        val widget = TabWidget.STATS
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
            lastStats = null
            return
        }

        firstInfoSeenTime = 0L

        val currentRaw = TabData.parseWidgetData(widget.lines)
        if (currentRaw == null || currentRaw == lastStats) return

        rawStats = currentRaw
        lastStats = currentRaw
        nextAllowedTime = now + 3_000L // same as Hypixel

        rawStats.joinToString(" | ") { it }
    }
}