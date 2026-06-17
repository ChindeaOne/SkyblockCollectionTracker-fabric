package io.github.chindeaone.collectiontracker.autoupdate

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.categories.About
import io.github.chindeaone.modrinthautoupdater.UpdateContext
import io.github.chindeaone.modrinthautoupdater.UpdateSetup
import io.github.chindeaone.modrinthautoupdater.UpdateTarget
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture

object UpdaterManager {

    private var activePromise: CompletableFuture<*>? = null
    private var potentialUpdate: UpdateSetup? = null
    private val version = SkyblockCollectionTracker.MC_VERSION
    private val logger: Logger = LogManager.getLogger(UpdaterManager::class.java)

    private val context = UpdateContext(
        SkyblockCollectionTracker.NAMESPACE,
        version,
        SkyblockCollectionTracker.VERSION,
        "none",
        SkyblockCollectionTracker.MODID,
        UpdateTarget.deleteAndSaveInTheSameFolder(UpdaterManager::class.java)
    )

    init {
        context.cleanup()
    }

    private fun setUpdateStream(): String {
        val currentStream = ConfigAccess.getUpdateType()
        return when (currentStream) {
            About.UpdateType.RELEASE -> "release"
            About.UpdateType.BETA -> "beta"
            else -> "none"
        }
    }

    fun update() {
        val stream = setUpdateStream()
        context.setStream(stream)
        activePromise = context.checkUpdate().thenAcceptAsync {update ->
            if (update == null) {
                logger.info("[SCT]: No compatible Modrinth update found for Minecraft $version.")
                return@thenAcceptAsync
            }
            potentialUpdate = update
            queueUpdate()
        }
    }

    private fun queueUpdate() {
        activePromise = CompletableFuture.supplyAsync {
            potentialUpdate!!.prepareUpdate()
        }.thenAcceptAsync {
            potentialUpdate!!.executeUpdate()
        }
    }
}