package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper

object TemporaryBuffsParser {

    fun resetRefinedCacao() {
        ConfigHelper.saveBuffTimes(
            System.currentTimeMillis() + 3600 * 1000,
            ConfigAccess.getFiletTime(),
            ConfigAccess.getPristinePotatoTime(),
            ConfigAccess.getPowderPumpkinTime()
        )
    }

    fun resetConsumable(name: String?) {
        val expiry = System.currentTimeMillis() + 3600 * 1000
        when (name) {
            "filet o' fortune" -> ConfigHelper.saveBuffTimes(ConfigAccess.getRefinedCacaoTime(), expiry, ConfigAccess.getPristinePotatoTime(), ConfigAccess.getPowderPumpkinTime())
            "chilled pristine potato" -> ConfigHelper.saveBuffTimes(ConfigAccess.getRefinedCacaoTime(), ConfigAccess.getFiletTime(), expiry, ConfigAccess.getPowderPumpkinTime())
            "powder pumpkin" -> ConfigHelper.saveBuffTimes(ConfigAccess.getRefinedCacaoTime(), ConfigAccess.getFiletTime(), ConfigAccess.getPristinePotatoTime(), expiry)
        }
    }
}