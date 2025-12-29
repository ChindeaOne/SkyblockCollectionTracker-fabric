package io.github.chindeaone.collectiontracker

import io.github.chindeaone.collectiontracker.config.ConfigManager
import io.github.chindeaone.collectiontracker.util.ModulesLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import java.util.ArrayList
import java.util.logging.Logger

object SkyblockCollectionTracker {

    val logger: Logger = Logger.getLogger("SkyblockCollectionTracker")

    fun preInit() {
        ModulesLoader.modules.forEach { ModLoader.loadModule(it) }

        logger.info("[SCT]: Skyblock Collection Tracker pre-initialization complete.")
    }

    fun init() {
        configManager = ConfigManager()

        Runtime.getRuntime().addShutdownHook(
            Thread { configManager.save() },
        )
    }

    fun onTick() {
        val screenToOpen = screenToOpen ?: return
        screenTicks++
        if (screenTicks != 5) return
        Minecraft.getInstance().setScreen(screenToOpen)
        screenTicks = 0
        this.screenToOpen = null
    }

    var screenToOpen: Screen? = null
    private var screenTicks = 0

    val modules: MutableList<Any> = ArrayList()
    lateinit var configManager: ConfigManager

    const val VERSION: String = "1.0.8"



//	override fun onInitialize() {
//		// This code runs as soon as Minecraft is in a mod-load-ready state.
//		// However, some things (like resources) may still be uninitialized.
//		// Proceed with mild caution.
//		logger.info("Hello Fabric world!")
//	}
}