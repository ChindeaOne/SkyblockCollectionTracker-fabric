package io.github.chindeaone.collectiontracker.utils.chat

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.stopwatchNotifier
import io.github.chindeaone.collectiontracker.config.stopwatchNotifierInterval
import io.github.chindeaone.collectiontracker.config.timerNotifier
import io.github.chindeaone.collectiontracker.config.timerNotifierInterval
import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatCompact
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object TimeNotifierUtils {

    private var lastTimerNotifier: EpochMark? = null
    private var lastStopwatchNotifier: EpochMark? = null

    fun onClientTick() {
        if (ModLoader.clientTicks % 2L != 0L) return

        val now = EpochMark.now()

        if (timerNotifier) {
            val timerOverlay = OverlayManager.getTimerOverlay()

            if (timerOverlay != null && !timerOverlay.hasEnded) {
                lastTimerNotifier?.let {
                    if (it.timePassedSince() >= timerNotifierInterval.minutes) {
                        lastTimerNotifier = now
                        sendTimerToParty(timerOverlay.getRemainingTimeInSeconds(), timerOverlay.isPaused)
                    }
                }
            }
        }

        if (stopwatchNotifier) {
            val stopwatchOverlay = OverlayManager.getStopwatchOverlay()

            if (stopwatchOverlay != null && stopwatchOverlay.stopwatchRunning) {
                lastStopwatchNotifier?.let {
                    if (it.timePassedSince() >= stopwatchNotifierInterval.minutes) {
                        lastStopwatchNotifier = now
                        sendStopwatchToParty(stopwatchOverlay.getElapsedTimeInSeconds(), stopwatchOverlay.stopwatchPaused)
                    }
                }
            }
        }
    }

    fun sendTimerToParty(time: Duration, isPaused: Boolean) {
        val message = "[SCT-Timer] Time left: ${time.formatCompact()}${if (isPaused) " (Paused)" else ""}"
        ChatUtils.sendHypixelCommand(message)
    }

    fun sendStopwatchToParty(time: Duration, isPaused: Boolean) {
        val message = "[SCT-Stopwatch] Elapsed time: ${time.formatCompact()}${if (isPaused) " (Paused)" else ""}"
        ChatUtils.sendHypixelCommand(message)
    }

    fun resetTimerNotifier() {
        lastTimerNotifier = EpochMark.now()
    }

    fun resetStopwatchNotifier() {
        lastStopwatchNotifier = EpochMark.now()
    }
}