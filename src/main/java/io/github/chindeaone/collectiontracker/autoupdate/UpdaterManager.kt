package io.github.chindeaone.collectiontracker.autoupdate

import io.github.chindeaone.UpdateContext
import io.github.chindeaone.UpdateSetup
import io.github.chindeaone.UpdateTarget
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.categories.About
import java.util.concurrent.CompletableFuture

object UpdaterManager {

    private var activePromise: CompletableFuture<*>? = null
    private var potentialUpdate: UpdateSetup? = null
    private val version = SkyblockCollectionTracker.MC_VERSION

    private val context = UpdateContext(
        "sct",
        version,
        SkyblockCollectionTracker.VERSION,
        "none",
        SkyblockCollectionTracker.MODID,
        UpdateTarget.deleteAndSaveInTheSameFolder(UpdaterManager::class.java)
    )

    init{
        context.cleanup()
    }

    private fun setUpdateStream(): String {
        val currentStream = SkyblockCollectionTracker.configManager.config!!.about.update
        return when (currentStream) {
            About.UpdateType.FULL -> "release"
            About.UpdateType.BETA -> "beta"
            else -> "none"
        }
    }

    fun update() {
        val stream = setUpdateStream()
        context.setStream(stream)
        activePromise = context.checkUpdate().thenAcceptAsync {
            potentialUpdate = it
            queueUpdate()
        }
    }

    private fun queueUpdate(){
        activePromise = CompletableFuture.supplyAsync{
            potentialUpdate!!.prepareUpdate()
        }.thenAcceptAsync {
            potentialUpdate!!.executeUpdate()
        }
    }
}