package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigManager.Companion.gson
import io.github.chindeaone.collectiontracker.utils.AbilityUtils.Pet
import io.github.chindeaone.collectiontracker.utils.AbilityUtils.lastPet
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import io.github.chindeaone.collectiontracker.utils.parser.TemporaryBuffsParser
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.jvm.java

object ConfigUtils {

    private val logger: Logger = LogManager.getLogger(AbilityUtils::class.java)

    fun loadFromConfig() {
        val savedPetJson = ConfigAccess.getLastPet()
        if (savedPetJson.isNotBlank()) {
            try {
                val loadedPet = gson.fromJson(savedPetJson, Pet::class.java)
                lastPet = loadedPet.copy(timestamp = System.currentTimeMillis())
            } catch (e: Exception) {
                logger.warn("[SCT]: Failed to load last pet from config: ${e.message}")
            }
        }
        val lastSkyMallBuff = ConfigAccess.getLastSkyMallBuff()
        val lastLotteryBuff = ConfigAccess.getLastLotteryBuff()
        val lastBeekeeperBuff = ConfigAccess.getLastBeekeeperBuff()

        if (lastSkyMallBuff.isNotBlank()) {
            ChatListener.currentSkyMallBuff = lastSkyMallBuff
            if (lastSkyMallBuff.contains("Cooldown")) ChatListener.isPickaxeAbility = true
        }
        if (lastLotteryBuff.isNotBlank()) {
            ChatListener.currentLotteryBuff = lastLotteryBuff
        }
        if (lastBeekeeperBuff.isNotBlank()) {
            ChatListener.currentBeekeeperBuff = lastBeekeeperBuff
        }

        TemporaryBuffsParser.loadDurations()
    }
}