package io.github.chindeaone.collectiontracker.util.tab

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.util.HypixelUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel

object TabData {

    private var tabCache: List<String> = emptyList()
    private var world: ClientLevel? = null

    fun tickAndUpdateWidget(client: Minecraft) {
        val currentWorld = client.level
        if (currentWorld == null) {
            world = null
            return
        }

        if (world != currentWorld) {
            world = currentWorld
        }

        if (!HypixelUtils.isOnSkyblock) return
        if (!ConfigAccess.isMiningStatsEnabled() && !ConfigAccess.isCommissionsEnabled() && !ConfigAccess.isForagingStatsOverlayEnabled()) return
        val newList = readTab() ?: return
        if (newList.isEmpty()) return

        tabCache = newList
        TabWidget.update(tabCache)

        CommissionsWidget.onTabWidgetsUpdate()
        MiningStatsWidget.onTabWidgetsUpdate()
        ForagingStatsWidget.onTabWidgetsUpdate()
    }

    private fun readTab(): List<String>? {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return null
        val connection = player.connection ?: return null

        val tabOverlay = mc.gui.tabList

        val result = connection.onlinePlayers
            .sortedWith(compareBy({ it.team?.name ?: "" }, { it.profile.name }))
            .map { tabOverlay.getNameForDisplay(it).string }


        return if (result.size > 80) result.subList(0, 80) else result
    }

    fun parseWidgetData(lines: List<String>): List<String>? {
        if (lines.size < 2) return null

        val body = lines.drop(1)
            .map { it.stripMinecraftFormatting().trim() }
            .filter { it.isNotEmpty() }

        return body.ifEmpty { null }
    }

    private fun String.stripMinecraftFormatting(): String =
        replace(Regex("§."), "")
}