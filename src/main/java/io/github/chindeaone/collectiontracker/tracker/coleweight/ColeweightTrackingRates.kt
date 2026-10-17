package io.github.chindeaone.collectiontracker.tracker.coleweight

import io.github.chindeaone.collectiontracker.gui.overlays.ColeweightOverlay
import io.github.chindeaone.collectiontracker.utils.EpochMark

object ColeweightTrackingRates {
    var coleweightAmount: Float = 0f
    var coleweightGained: Float = 0f
    var coleweightPerHour: Float = 0f
    var coleweightSinceLast: Float = 0f
    var lastColeweightUpdate: EpochMark? = null

    private var startColeweight: Float = -1f

    fun calculateRates(currentValue: Float) {
        if (startColeweight == -1f) {
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
            (coleweightGained / (uptime / 3600.0)).toFloat()
        } else 0f

        if (!ColeweightOverlay.trackingDirty) ColeweightOverlay.trackingDirty = true
    }

    fun reset() {
        coleweightAmount = 0f
        coleweightGained = 0f
        coleweightPerHour = 0f
        coleweightSinceLast = 0f
        lastColeweightUpdate = null
        startColeweight = -1f
    }
}