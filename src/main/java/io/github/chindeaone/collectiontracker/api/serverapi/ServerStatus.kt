package io.github.chindeaone.collectiontracker.api.serverapi

import io.github.chindeaone.collectiontracker.api.ApiManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executor

object ServerStatus {

    private val logger: Logger = LogManager.getLogger(ServerStatus::class.java)

    fun checkServerWithCallback(callbackExecutor: Executor, callback: (Boolean) -> Unit) {
        ApiManager.checkServer("status")
            .thenAcceptAsync({ result ->
                try {
                    callback(result)
                } catch (e: Exception) {
                    logger.error("[SCT]: Error in server status callback", e)
                }
            }, callbackExecutor)
    }
}