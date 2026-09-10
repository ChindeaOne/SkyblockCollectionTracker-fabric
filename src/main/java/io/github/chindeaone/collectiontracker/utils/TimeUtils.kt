package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils

object TimeUtils {

    private var lastTimerNotifier: Long = 0
    private var lastStopwatchNotifier: Long = 0

    fun onClientTick() {
        if (ModLoader.clientTicks % 4L != 0L) return

        val now = System.currentTimeMillis()

        if (ConfigAccess.isTimerNotifierEnabled()) {
            val timerOverlay = OverlayManager.getTimerOverlay()

            if (timerOverlay != null && !timerOverlay.hasEnded) {
                val interval = ConfigAccess.getTimerNotifierInterval() * 60_000L

                if (now - lastTimerNotifier >= interval) {
                    lastTimerNotifier = now
                    sendTimerToParty(timerOverlay.getRemainingTimeInSeconds(), timerOverlay.isPaused)
                }
            }
        }

        if (ConfigAccess.isStopwatchNotifierEnabled()) {
            val stopwatchOverlay = OverlayManager.getStopwatchOverlay()

            if (stopwatchOverlay != null && stopwatchOverlay.stopwatchRunning) {
                val interval = ConfigAccess.getStopwatchNotifierInterval() * 60_000L

                if (now - lastStopwatchNotifier >= interval) {
                    lastStopwatchNotifier = now
                    sendStopwatchToParty(stopwatchOverlay.getElapsedTimeInSeconds(), stopwatchOverlay.stopwatchPaused)
                }
            }
        }
    }

    fun sendTimerToParty(time: Long, isPaused: Boolean) {
        val message = "Timer] §eTime left: ${StringUtils.formatCompactTime(time)}${if (isPaused) " §c(Paused)" else ""}"
        ChatUtils.sendHypixelCommand(message)
    }

    fun sendStopwatchToParty(time: Long, isPaused: Boolean) {
        val message = "Stopwatch] §eElapsed time: ${StringUtils.formatCompactTime(time)}${if (isPaused) " §c(Paused)" else ""}"
        ChatUtils.sendHypixelCommand(message)
    }

    fun resetTimerNotifier() {
        lastTimerNotifier = System.currentTimeMillis()
    }

    fun resetStopwatchNotifier() {
        lastStopwatchNotifier = System.currentTimeMillis()
    }
}