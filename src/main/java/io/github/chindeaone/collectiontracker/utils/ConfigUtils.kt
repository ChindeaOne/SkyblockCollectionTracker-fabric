package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.jvm.java

object ConfigUtils {

    private val logger: Logger = LogManager.getLogger(AbilityUtils::class.java)

    fun loadFromConfig() {
        val lastSkyMallBuff = ConfigAccess.getLastSkyMallBuff()
        val lastLotteryBuff = ConfigAccess.getLastLotteryBuff()
        val lastBeekeeperBuff = ConfigAccess.getLastBeekeeperBuff()

        if (lastSkyMallBuff.isNotBlank()) {
            ChatListener.currentSkyMallBuff = lastSkyMallBuff
            if (lastSkyMallBuff.contains("Cooldown")) ChatListener.isPickaxeAbility = true
            logger.info("[SCT]: Loaded last SkyMall buff from config: {}", lastSkyMallBuff)
        }
        if (lastLotteryBuff.isNotBlank()) {
            ChatListener.currentLotteryBuff = lastLotteryBuff
            logger.info("[SCT]: Loaded last Lottery buff from config: {}", lastLotteryBuff)
        }
        if (lastBeekeeperBuff.isNotBlank()) {
            ChatListener.currentBeekeeperBuff = lastBeekeeperBuff
            logger.info("[SCT]: Loaded last Beekeeper buff from config: {}", lastBeekeeperBuff)
        }

        TemporaryBuffsParser.loadDurations()
    }
}