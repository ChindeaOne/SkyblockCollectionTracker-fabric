package io.github.chindeaone.collectiontracker

import io.github.chindeaone.collectiontracker.config.ConfigManager
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import java.util.logging.Logger

object SkyblockCollectionTracker {

    val logger: Logger = Logger.getLogger("SkyblockCollectionTracker")

    fun init() {
        configManager = ConfigManager()

        Runtime.getRuntime().addShutdownHook(
            Thread { configManager.save() },
        )
    }
    fun onTick(client: Minecraft) {
        val screenToOpen = screenToOpen ?: return
        screenTicks++
        if (screenTicks != 5) return
        shouldCloseScreen = true
        client.setScreen(screenToOpen)
        screenTicks = 0
        this.screenToOpen = null
    }

    var screenToOpen: Screen? = null
    var shouldCloseScreen: Boolean = true
    private var screenTicks = 0

    lateinit var configManager: ConfigManager

    const val NAMESPACE: String = "sct"
    const val MODID = "skyblockcollectiontracker"

    @JvmField
    val VERSION: String = FabricLoader.getInstance().getModContainer(MODID).orElseThrow().metadata.version.friendlyString
    @JvmField
    val MC_VERSION: String = SharedConstants.getCurrentVersion().name()
}