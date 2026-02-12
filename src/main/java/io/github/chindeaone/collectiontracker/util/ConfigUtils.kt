package io.github.chindeaone.collectiontracker.util

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigManager.Companion.gson
import io.github.chindeaone.collectiontracker.util.AbilityUtils.Pet
import io.github.chindeaone.collectiontracker.util.AbilityUtils.lastPet
import io.github.chindeaone.collectiontracker.util.chat.ChatListener
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
        val lastSkyMallPerk = ConfigAccess.getLastSkyMallPerk()
        val lastLotteryPerk = ConfigAccess.getLastLotteryPerk()

        if (lastSkyMallPerk.isNotBlank()) {
            ChatListener.currentSkyMallBuff = lastSkyMallPerk
        }
        if (lastLotteryPerk.isNotBlank()) {
            ChatListener.currentLotteryBuff = lastLotteryPerk
        }
    }
}