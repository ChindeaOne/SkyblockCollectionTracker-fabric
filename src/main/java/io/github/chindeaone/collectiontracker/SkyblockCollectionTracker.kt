package io.github.chindeaone.collectiontracker

import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.commands.CommandRegistry
import io.github.chindeaone.collectiontracker.config.ConfigManager
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen

object SkyblockCollectionTracker {

    fun init() {
        PlayerData.init()
        CommandRegistry.init()

        configInit()

        ServerUtils.startCheckingServer()

        Runtime.getRuntime().addShutdownHook(
            Thread { configManager.save() }
        )

        Runtime.getRuntime().addShutdownHook(
            Thread { ApiManager.removePlayer() }
        )
    }

    private fun configInit(){
        configManager = ConfigManager()
        configManager.loadFromConfig()
        configManager.startAutoSave()
    }

    fun onClientTick(client: Minecraft) {
        val screenToOpen = screenToOpen ?: return
        if (ModLoader.clientTicks % 5 != 0L) return
        shouldCloseScreen = true
        client./*? if 26.2 {*/ /*gui.setScreen *//*?} else {*/ setScreen /*?}*/(screenToOpen)
        this.screenToOpen = null
    }

    var screenToOpen: Screen? = null
    var shouldCloseScreen: Boolean = true

    lateinit var configManager: ConfigManager

    const val NAMESPACE: String = "sct"
    const val MODID = "skyblockcollectiontracker"

    @JvmField
    val VERSION: String = FabricLoader.getInstance().getModContainer(MODID).orElseThrow().metadata.version.friendlyString
    @JvmField
    val MC_VERSION: String = SharedConstants.getCurrentVersion().name()
}