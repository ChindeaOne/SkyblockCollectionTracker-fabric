package io.github.chindeaone.collectiontracker.api.skilltreeapi

import com.google.gson.JsonParser
import io.github.chindeaone.collectiontracker.api.ApiManager
import io.github.chindeaone.collectiontracker.api.tokenapi.TokenManager
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.PlayerData
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.http.HttpResponse
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object FetchSkillTree {

    private val logger: Logger = LogManager.getLogger(FetchSkillTree::class.java)
    @Volatile
    var hasSkillTree: Boolean = false
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    private var pendingHotmReset: ScheduledFuture<*>? = null
    private var pendingHotfReset: ScheduledFuture<*>? = null

    fun fetchSkillTree(mining: Boolean, foraging: Boolean) {
        if (alreadyHasData()) {
            hasSkillTree = true
            logger.info("[SCT]: Skill tree data already exists. Skipping fetch.")
            return
        }

        try {
            val headers = listOf(
                "Authorization" to "Bearer ${TokenManager.token}",
                "X-UUID" to PlayerData.playerUUID,
                "X-MINING" to mining.toString(),
                "X-FORAGING" to foraging.toString()
            )
            val response = ApiManager.request("skill-tree", headers)

            when (val status = response.statusCode()) {
                401 -> {
                    logger.warn("[SCT]: Invalid or expired token. Fetching a new one and retrying...")
                    TokenManager.fetchAndStoreToken()

                    val headersWithNewToken = listOf(
                        "Authorization" to "Bearer ${TokenManager.token}",
                        "X-UUID" to PlayerData.playerUUID,
                        "X-MINING" to mining.toString(),
                        "X-FORAGING" to foraging.toString()
                    )
                    val responseWithNewToken = ApiManager.request("skill-tree", headersWithNewToken)

                    val statusWithNewToken = responseWithNewToken.statusCode()

                    if (statusWithNewToken == 200) {
                        processSuccessfulResponse(responseWithNewToken)
                    } else {
                        logger.error("[SCT]: Failed to fetch skill tree data after token refresh. Server responded with code: {}", statusWithNewToken)
                    }
                }

                200 -> processSuccessfulResponse(response)

                else -> {
                    logger.error("[SCT]: Failed to fetch skill tree data. Server responded with code: {}", status)
                }
            }
        } catch (e: Exception) {
            logger.error("[SCT]: An error occurred while fetching skill tree data: ", e)
        }
    }

    private fun processSuccessfulResponse(response: HttpResponse<String>) {
        try {
            val jsonObject = JsonParser.parseString(response.body()).asJsonObject

            jsonObject.getAsJsonObject("mining")?.let { miningTree ->
                miningTree.get("core_of_the_mountain")?.let {
                    ConfigHelper.setCotmLevel(it.asInt)
                }
                miningTree.get("professional")?.let {
                    ConfigHelper.setProfessionalMS(it.asInt)
                }
                miningTree.get("strong_arm")?.let {
                    ConfigHelper.setStrongArmMS(it.asInt)
                }
            }

            jsonObject.getAsJsonObject("foraging")?.let { foragingTree ->
                foragingTree.get("center_of_the_forest")?.let {
                    ConfigHelper.setCotfLevel(it.asInt)
                }
            }

            hasSkillTree = true
            logger.info("[SCT]: Successfully fetched skill tree data.")
        } catch (e: Exception) {
            logger.error("[SCT]: Error processing skill tree data: ", e)
        }
    }

    fun resetHotm() {
        ConfigHelper.setStrongArmMS(0)
        ConfigHelper.setProfessionalMS(0)
        ConfigHelper.setLastSkyMallBuff("")
        ChatListener.currentSkyMallBuff = ""

        if (pendingHotmReset == null || pendingHotmReset?.isDone != false) {
            pendingHotmReset = scheduler.schedule({
                fetchSkillTree(mining = true, foraging = false)
            }, 10, TimeUnit.MINUTES)

            logger.info("[SCT]: Scheduled a mining skill tree fetch in 10 minutes.")
        }
    }

    fun resetHotf() {
        ConfigHelper.setLastLotteryBuff("")
        ConfigHelper.setLastBeekeeperBuff("")

        ChatListener.currentLotteryBuff = ""
        ChatListener.currentBeekeeperBuff = ""

        if (pendingHotfReset == null || pendingHotfReset?.isDone != false) {
            pendingHotfReset = scheduler.schedule({
                fetchSkillTree(mining = false, foraging = true)
            }, 10, TimeUnit.MINUTES)

            logger.info("[SCT]: Scheduled a foraging skill tree fetch in 10 minutes.")
        }
    }

    private fun alreadyHasData(): Boolean {
        return ConfigAccess.getCotmLevel() != 0 &&
                ConfigAccess.getProfessionalMS() != 0 &&
                ConfigAccess.getStrongArmMS() != 0 &&
                ConfigAccess.getCotfLevel() != 0
    }
}
