package io.github.chindeaone.collectiontracker.util.tab

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.util.HypixelUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel

object TabData {

    private var tabCache: List<String> = emptyList()
    private val isEnabled: Boolean get() = SkyblockCollectionTracker.configManager.config?.mining?.commissionsOverlay?.enableCommissionsOverlay ?: false
    private var world: ClientLevel? = null

    fun tickAndUpdateWidget() {
        val currentWorld = Minecraft.getInstance().level
        if (currentWorld == null) {
            world = null
            return
        }

        if (world != currentWorld) {
            world = currentWorld
        }

        if (!HypixelUtils.isOnSkyblock || !isEnabled) return
        val newList = readTab() ?: return
        if (newList.isEmpty()) return

        tabCache = newList
        TabWidget.update(tabCache)
        CommissionsWidget.onTabWidgetsUpdate()
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
}