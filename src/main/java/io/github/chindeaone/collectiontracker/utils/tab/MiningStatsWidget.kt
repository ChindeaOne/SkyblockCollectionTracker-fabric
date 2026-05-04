package io.github.chindeaone.collectiontracker.utils.tab

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping
import io.github.chindeaone.collectiontracker.utils.world.WaypointsUtils

object MiningStatsWidget {

    private var lastStats: List<String>? = null
    var rawStats: List<String> = emptyList()

    private var nextAllowedTime: Long = 0L
    private var firstInfoSeenTime: Long = 0L
    private var wasReset: Boolean = false

    private var lastMineshaftEnabled = false
    private var lastMetalEnabled = false
    private var lastOresEnabled = false

    fun onTabWidgetsUpdate() {
        val now = System.currentTimeMillis()
        if (now < nextAllowedTime) return

        val currentMiningIsland = IslandTracker.currentMiningIsland
        if (currentMiningIsland == null) {
            rawStats = emptyList()
            lastStats = null
            return
        }

        if (currentMiningIsland == "Mineshaft") {
            if (!wasReset) {
                ChatListener.resetPickaxeAbilities()
                wasReset = true
            }
        } else {
            wasReset = false
        }

        if (currentMiningIsland == "Dwarven Mines") {
            val routes = listOf(
                Triple(ConfigAccess.isMineshaftSpawnRoutesEnabled(), lastMineshaftEnabled, ConfigAccess::setMineshaftSpawnRoutesEnabled),
                Triple(ConfigAccess.isDwarvenMetalRoutesEnabled(), lastMetalEnabled, ConfigAccess::setDwarvenMetalRoutesEnabled),
                Triple(ConfigAccess.isPureOresRoutesEnabled(), lastOresEnabled, ConfigAccess::setPureOresRoutesEnabled)
            )

            val selectedIndex = routes.indexOfFirst { it.first && !it.second }

            if (selectedIndex != -1) {
                val otherEnabled = routes.indices.any { it != selectedIndex && routes[it].second }
                if (otherEnabled) {
                    ChatUtils.sendMessage("§cCannot enable another route. Disable the current one first.", true)
                    routes[selectedIndex].third(false)
                }
            }

            lastMineshaftEnabled = ConfigAccess.isMineshaftSpawnRoutesEnabled()
            lastMetalEnabled = ConfigAccess.isDwarvenMetalRoutesEnabled()
            lastOresEnabled = ConfigAccess.isPureOresRoutesEnabled()

            val category = when {
                lastMineshaftEnabled -> ConfigAccess.getMineshaftSpawnRoutes().type
                lastMetalEnabled -> ConfigAccess.getDwarvenMetalRoutes().type
                lastOresEnabled -> ConfigAccess.getPureOresRoutes().type
                else -> null
            }

            if (category != null) {
                WaypointsUtils.selectCategory(category)
            } else {
                WaypointsUtils.currentCategory = null
                WaypointsUtils.reset()
            }
        } else if (currentMiningIsland != "Mineshaft") {
            WaypointsUtils.currentCategory = null
        }

        if (!ConfigAccess.isMiningStatsEnabled()) {
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