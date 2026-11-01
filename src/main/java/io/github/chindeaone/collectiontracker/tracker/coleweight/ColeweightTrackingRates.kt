package io.github.chindeaone.collectiontracker.tracker.coleweight

import io.github.chindeaone.collectiontracker.gui.overlays.ColeweightOverlay
import io.github.chindeaone.collectiontracker.utils.EpochMark

object ColeweightTrackingRates {
    var coleweightAmount: Double = 0.0
    var coleweightGained: Double = 0.0
    var coleweightPerHour: Double = 0.0
    var coleweightSinceLast: Double = 0.0
    var lastColeweightUpdate: EpochMark? = null

    private var startColeweight: Double = -1.0

    fun calculateRates(currentValue: Double) {
        if (startColeweight == -1.0) {
            startColeweight = currentValue
            coleweightAmount = currentValue
            lastColeweightUpdate = EpochMark.now()
        }

        coleweightSinceLast = currentValue - coleweightAmount
        lastColeweightUpdate = EpochMark.now()

        coleweightAmount = currentValue

        coleweightGained = currentValue - startColeweight
        val uptime = ColeweightTrackingHandler.uptimeInSeconds

        coleweightPerHour = if (uptime > 0) {
            (coleweightGained / (uptime / 3600.0))
        } else 0.0

        if (!ColeweightOverlay.trackingDirty) ColeweightOverlay.trackingDirty = true
    }

    fun reset() {
        coleweightAmount = 0.0
        coleweightGained = 0.0
        coleweightPerHour = 0.0
        coleweightSinceLast = 0.0
        lastColeweightUpdate = null
        startColeweight = -1.0
    }
}