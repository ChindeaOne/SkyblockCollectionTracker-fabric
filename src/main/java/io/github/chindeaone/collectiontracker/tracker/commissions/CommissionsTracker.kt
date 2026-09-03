package io.github.chindeaone.collectiontracker.tracker.commissions

import io.github.chindeaone.collectiontracker.utils.StringUtils

object CommissionsTracker {
    private var completedCount = 0
    private var startTime = 0L

    private var cachedPerHour = 0.0
    private var cachedUptime = "00:00:00"
    private var lastUpdate = 0L

    fun onCommissionClaimed() {
        if (completedCount == 0) {
            startTime = System.currentTimeMillis()
        }
        completedCount++
        update(true)
    }

    private fun update(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastUpdate < 1000L) return

        lastUpdate = now

        val durationMs = now - startTime
        if (durationMs > 0) {
            val hours = durationMs.toDouble() / (1000.0 * 60.0 * 60.0)
            cachedPerHour = completedCount / hours
        }

        cachedUptime = StringUtils.formatTime(durationMs / 1000L)
    }

    fun getCompletedCount(): Int = completedCount

    fun getCommissionsPerHour(): Double {
        update()
        return cachedPerHour
    }

    fun getUptime(): String {
        update()
        return cachedUptime
    }

    fun reset() {
        completedCount = 0
        startTime = 0L
    }
}

