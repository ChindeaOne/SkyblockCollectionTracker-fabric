package io.github.chindeaone.collectiontracker.api.serverapi

import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.coleweight.ColeweightFetcher
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchCollectionList
import io.github.chindeaone.collectiontracker.api.collectionapi.FetchGemstoneList
import io.github.chindeaone.collectiontracker.api.colors.FetchColors
import io.github.chindeaone.collectiontracker.api.eliteapi.EliteApiFetcher
import io.github.chindeaone.collectiontracker.api.npcpriceapi.FetchNpcPrices
import io.github.chindeaone.collectiontracker.api.skilltreeapi.FetchSkillTree
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

    @Synchronized
    fun hasData(): Boolean {
        return FetchColors.hasColors &&
                FetchNpcPrices.hasNpcPrice &&
                FetchCollectionList.hasCollectionList &&
                FetchGemstoneList.hasGemstoneList &&
                FetchSkillTree.hasSkillTree &&
                ColeweightFetcher.hasColeweightTopColors &&
                ColeweightFetcher.hasColeweightLb &&
                EliteApiFetcher.hasFarmingweightTopColors &&
                EliteApiFetcher.hasFarmingweightLb
    }
}