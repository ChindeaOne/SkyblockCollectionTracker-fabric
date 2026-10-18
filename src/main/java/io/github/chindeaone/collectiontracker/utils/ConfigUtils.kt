package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.config.axeAbilityName
import io.github.chindeaone.collectiontracker.config.lastBeekeeperBuff
import io.github.chindeaone.collectiontracker.config.lastLotteryBuff
import io.github.chindeaone.collectiontracker.config.lastSkyMallBuff
import io.github.chindeaone.collectiontracker.config.pickaxeAbilityName
import io.github.chindeaone.collectiontracker.utils.ability.Ability
import io.github.chindeaone.collectiontracker.utils.ability.AbilityUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import io.github.chindeaone.collectiontracker.utils.parser.ConsumableParser
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object ConfigUtils {

    private val logger: Logger = LogManager.getLogger(ConfigUtils::class)

    fun loadRotatingHotxPerks() {
        val lastSkyMallBuff = lastSkyMallBuff
        val lastLotteryBuff = lastLotteryBuff
        val lastBeekeeperBuff = lastBeekeeperBuff

        if (lastSkyMallBuff.isNotBlank()) {
            ChatListener.currentSkyMallBuff = lastSkyMallBuff
            if (lastSkyMallBuff.contains("Pickaxe")) AbilityUtils.isPickaxeAbility = true
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
    }

    fun loadConsumableDurations() {
        ConsumableParser.loadConsumableDurations()
    }

    fun loadAbilities() {
        val pickaxeAbility = Ability.fromName(pickaxeAbilityName)
        if (pickaxeAbility != null) {
            AbilityUtils.currentPickaxeAbility = pickaxeAbility
            logger.info("[SCT]: Loaded pickaxe ability from config: {}", pickaxeAbility.abilityName)
        }

        val axeAbility = Ability.fromName(axeAbilityName)
        if (axeAbility != null) {
            AbilityUtils.currentAxeAbility = axeAbility
            logger.info("[SCT]: Loaded axe ability from config: {}", axeAbility.abilityName)
        }
    }
}