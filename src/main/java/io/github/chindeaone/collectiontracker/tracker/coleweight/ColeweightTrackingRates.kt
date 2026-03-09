package io.github.chindeaone.collectiontracker.tracker.coleweight

import io.github.chindeaone.collectiontracker.gui.overlays.ColeweightOverlay

object ColeweightTrackingRates {
    @JvmStatic
    var coleweightAmount = 0f
        private set

    @JvmStatic
    var coleweightGained = 0f
        private set

    @JvmStatic
    var coleweightPerHour = 0f
        private set

    @JvmStatic
    var coleweightSinceLast = 0f
        private set

    @JvmStatic
    var lastColeweightTime = -1L
        private set

    @JvmStatic
    var afk = false

    private var startColeweight = -1f
    private var unchangedStreak = 0
    private const val THRESHOLD = 2

    @JvmStatic
    fun calculateRates(currentValue: Float) {
        if (startColeweight == -1f) {
            startColeweight = currentValue
            coleweightAmount = currentValue
            lastColeweightTime = System.currentTimeMillis()
        }

        coleweightSinceLast = currentValue - coleweightAmount
        lastColeweightTime = System.currentTimeMillis()

        if (currentValue == coleweightAmount && currentValue != 0f) {
            unchangedStreak++
            if (unchangedStreak >= THRESHOLD) {
                afk = true
                ColeweightTrackingHandler.stopTracking()
                unchangedStreak = 0
                return
            }
        } else {
            unchangedStreak = 0
            afk = false
        }

        coleweightAmount = currentValue

        coleweightGained = currentValue - startColeweight
        val uptime = ColeweightTrackingHandler.getUptimeInSeconds()
        coleweightPerHour = if (uptime > 0) {
            (coleweightGained / (uptime / 3600.0)).toFloat()
        } else {
            0f
        }

        if (!ColeweightOverlay.trackingDirty) ColeweightOverlay.trackingDirty = true
    }

    @JvmStatic
    fun reset() {
        coleweightAmount = 0f
        coleweightGained = 0f
        coleweightPerHour = 0f
        coleweightSinceLast = 0f
        lastColeweightTime = -1L
        startColeweight = -1f
        unchangedStreak = 0
        afk = false
    }
}