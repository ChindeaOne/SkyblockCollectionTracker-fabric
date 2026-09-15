package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.config.refinedCacaoTime
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.fiestaFlaskTime
import io.github.chindeaone.collectiontracker.config.filetTime
import io.github.chindeaone.collectiontracker.config.powderPumpkinTime
import io.github.chindeaone.collectiontracker.config.pristinePotatoTime
import kotlin.time.Duration.Companion.hours

object TemporaryBuffsParser {

    private val HOUR = 1.hours.inWholeMilliseconds

    var refinedCacaoEndTime: Long = 0L
    var filetEndTime: Long = 0L
    var pristinePotatoEndTime: Long = 0L
    var powderPumpkinEndTime: Long = 0L
    var fiestaFlaskEndTime: Long = 0L

    fun loadDurations() {
        val now = System.currentTimeMillis()
        refinedCacaoEndTime = now + refinedCacaoTime
        filetEndTime = now + filetTime
        pristinePotatoEndTime = now + pristinePotatoTime
        powderPumpkinEndTime = now + powderPumpkinTime
        fiestaFlaskEndTime = now + fiestaFlaskTime
    }

    fun saveDurations() {
        val now = System.currentTimeMillis()
        ConfigHelper.setDuration(
            refined = (refinedCacaoEndTime - now).coerceAtLeast(0L),
            filet = (filetEndTime - now).coerceAtLeast(0L),
            potato = (pristinePotatoEndTime - now).coerceAtLeast(0L),
            pumpkin = (powderPumpkinEndTime - now).coerceAtLeast(0L),
            fiesta = (fiestaFlaskEndTime - now).coerceAtLeast(0L)
        )
    }

    fun resetRefinedCacao() {
        ConfigHelper.setDuration(refined = HOUR)
        refinedCacaoEndTime = System.currentTimeMillis() + HOUR
    }

    fun resetConsumable(name: String?) {
        val now = System.currentTimeMillis()
        when (name) {
            "filet o' fortune" -> {
                ConfigHelper.setDuration(filet = HOUR)
                filetEndTime = now + HOUR
            }
            "chilled pristine potato" -> {
                ConfigHelper.setDuration(potato = HOUR)
                pristinePotatoEndTime = now + HOUR
            }
            "powder pie" -> {
                ConfigHelper.setDuration(pumpkin = HOUR)
                powderPumpkinEndTime = now + HOUR
            }
            "fiesta flask" -> {
                ConfigHelper.setDuration(fiesta = HOUR)
                fiestaFlaskEndTime = now + HOUR
            }
        }
    }
}