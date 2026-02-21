package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import kotlin.time.Duration.Companion.hours

object TemporaryBuffsParser {

    private val HOUR = 1.hours.inWholeMilliseconds

    @JvmStatic var refinedCacaoTime: Long = 0L
    @JvmStatic var filetTime: Long = 0L
    @JvmStatic var pristinePotatoTime: Long = 0L
    @JvmStatic var powderPumpkinTime: Long = 0L

    fun loadDurations() {
        val now = System.currentTimeMillis()
        refinedCacaoTime = now + ConfigAccess.getRefinedCacaoTime()
        filetTime = now + ConfigAccess.getFiletTime()
        pristinePotatoTime = now + ConfigAccess.getPristinePotatoTime()
        powderPumpkinTime = now + ConfigAccess.getPowderPumpkinTime()
    }

    fun saveDurations() {
        val now = System.currentTimeMillis()
        ConfigHelper.setDuration(
            refined = (refinedCacaoTime - now).coerceAtLeast(0L),
            filet = (filetTime - now).coerceAtLeast(0L),
            potato = (pristinePotatoTime - now).coerceAtLeast(0L),
            pumpkin = (powderPumpkinTime - now).coerceAtLeast(0L)
        )
    }

    fun resetRefinedCacao() {
        ConfigHelper.setDuration(refined = HOUR)
        refinedCacaoTime = System.currentTimeMillis() + HOUR
    }

    fun resetConsumable(name: String?) {
        val now = System.currentTimeMillis()
        when (name) {
            "filet o' fortune" -> {
                ConfigHelper.setDuration(filet = HOUR)
                filetTime = now + HOUR
            }
            "chilled pristine potato" -> {
                ConfigHelper.setDuration(potato = HOUR)
                pristinePotatoTime = now + HOUR
            }
            "powder pumpkin" -> {
                ConfigHelper.setDuration(pumpkin = HOUR)
                powderPumpkinTime = now + HOUR
            }
        }
    }
}