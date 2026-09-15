package io.github.chindeaone.collectiontracker.utils.tab

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.coleweightRankingInChat
import io.github.chindeaone.collectiontracker.config.displayAxeAbility
import io.github.chindeaone.collectiontracker.config.displayPickaxeAbility
import io.github.chindeaone.collectiontracker.config.enableCommissionsOverlay
import io.github.chindeaone.collectiontracker.config.enableDeployable
import io.github.chindeaone.collectiontracker.config.enableForagingStatsOverlay
import io.github.chindeaone.collectiontracker.config.enableLottery
import io.github.chindeaone.collectiontracker.config.enableMineshaftRoutes
import io.github.chindeaone.collectiontracker.config.enableMineshaftSpawnRoutes
import io.github.chindeaone.collectiontracker.config.enableMiningStatsOverlay
import io.github.chindeaone.collectiontracker.config.enableSkyMall
import io.github.chindeaone.collectiontracker.config.farmingweightRankingInChat
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.parser.DeployableParser
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import io.github.chindeaone.collectiontracker.utils.world.WaypointsUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.multiplayer.PlayerInfo

object TabData {

    private var tabCache: List<String> = emptyList()
    private var world: ClientLevel? = null
    var lastWorldSwitch: Long = 0L

    private val TAB_COMPARATOR = compareBy<PlayerInfo>(
        { it.team?.name ?: "" }, { it.profile.name }
    )

    fun onClientTick(client: Minecraft) {
        if (ModLoader.clientTicks % 4L != 0L) return

        val currentWorld = client.level
        if (currentWorld == null) {
            world = null
            return
        }

        if (world != currentWorld) {
            world = currentWorld
            IslandTracker.reset()
            DeployableParser.reset()
            WaypointsUtils.reset()
            StatsWidget.clearStats()
            lastWorldSwitch = System.currentTimeMillis()
        }

        if (!enableMiningStatsOverlay &&
            !enableCommissionsOverlay &&
            !enableForagingStatsOverlay &&
            !enableSkyMall &&
            !enableLottery &&
            !displayPickaxeAbility &&
            !displayAxeAbility &&
            !enableMineshaftRoutes &&
            !enableMineshaftSpawnRoutes &&
            !enableDeployable &&
            !coleweightRankingInChat &&
            !farmingweightRankingInChat) return

        val newTab = readTab(client) ?: return
        if (newTab.isEmpty()) return
        if (newTab == tabCache) return

        tabCache = newTab
        TabWidget.update(tabCache)

        IslandTracker.onTabUpdate()
        CommissionWidget.onTabUpdate()
        StatsWidget.onTabUpdate()
    }

    private fun readTab(client: Minecraft): List<String>? {
        val player = client.player ?: return null
        val connection = player.connection

        val tabOverlay = client.gui /*? if 26.2 {*/ /*.hud *//*?}*/.tabList

        val result = connection.onlinePlayers
            .sortedWith(TAB_COMPARATOR)
            .map { tabOverlay.getNameForDisplay(it).string }

        return if (result.size > 80) result.subList(0, 80) else result.dropLast(1)
    }

    fun parseWidgetData(lines: List<String>): List<String>? {
        val body = lines.drop(1)
            .map { it.removeColor().trim() }
            .filter { it.isNotEmpty() }

        return body.ifEmpty { null }
    }
}