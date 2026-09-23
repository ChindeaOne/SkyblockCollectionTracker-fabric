package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.stopwatchNotifier
import io.github.chindeaone.collectiontracker.config.stopwatchNotifierInterval
import io.github.chindeaone.collectiontracker.config.timerNotifier
import io.github.chindeaone.collectiontracker.config.timerNotifierInterval
import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils

object TimeUtils {

    private var lastTimerNotifier: Long = 0
    private var lastStopwatchNotifier: Long = 0

    fun onClientTick() {
        if (ModLoader.clientTicks % 4L != 0L) return

        val now = System.currentTimeMillis()

        if (timerNotifier) {
            val timerOverlay = OverlayManager.getTimerOverlay()

            if (timerOverlay != null && !timerOverlay.hasEnded) {
                val interval = timerNotifierInterval * 60_000L

                if (now - lastTimerNotifier >= interval) {
                    lastTimerNotifier = now
                    sendTimerToParty(timerOverlay.getRemainingTimeInSeconds(), timerOverlay.isPaused)
                }
            }
        }

        if (stopwatchNotifier) {
            val stopwatchOverlay = OverlayManager.getStopwatchOverlay()

            if (stopwatchOverlay != null && stopwatchOverlay.stopwatchRunning) {
                val interval = stopwatchNotifierInterval * 60_000L

                if (now - lastStopwatchNotifier >= interval) {
                    lastStopwatchNotifier = now
                    sendStopwatchToParty(stopwatchOverlay.getElapsedTimeInSeconds(), stopwatchOverlay.stopwatchPaused)
                }
            }
        }
    }

    fun sendTimerToParty(time: Long, isPaused: Boolean) {
        val message = "[SCT-Timer] Time left: ${StringUtils.formatCompactTime(time)}${if (isPaused) " (Paused)" else ""}"
        ChatUtils.sendHypixelCommand(message)
    }

    fun sendStopwatchToParty(time: Long, isPaused: Boolean) {
        val message = "[SCT-Stopwatch] Elapsed time: ${StringUtils.formatCompactTime(time)}${if (isPaused) " (Paused)" else ""}"
        ChatUtils.sendHypixelCommand(message)
    }

    fun resetTimerNotifier() {
        lastTimerNotifier = System.currentTimeMillis()
    }

    fun resetStopwatchNotifier() {
        lastStopwatchNotifier = System.currentTimeMillis()
    }
}