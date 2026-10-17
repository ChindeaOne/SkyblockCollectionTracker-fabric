package io.github.chindeaone.collectiontracker.tracker.commissions

import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.formatClock
import kotlin.time.Duration.Companion.seconds

object CommissionsTracker {
    private var completedCount: Int = 0
    private var startTime: EpochMark? = null

    private var cachedPerHour: Double = 0.0
    private var cachedUptime: String? = null
    private var lastUpdate: EpochMark? = null

    fun onCommissionClaimed() {
        if (completedCount == 0) {
            startTime = EpochMark.now()
        }
        completedCount++
        update(true)
    }

    private fun update(force: Boolean = false) {
        val now = EpochMark.now()
        if (!force && lastUpdate != null && lastUpdate!!.timePassedSince() < 1.seconds) return

        lastUpdate = now

        val start = startTime ?: return
        val duration = now - start

        if (duration >= 1.seconds) {
            val hours = duration.inWholeSeconds / 3600.0
            cachedPerHour = completedCount / hours
        } else cachedPerHour = 0.0

        cachedUptime = duration.formatClock()
    }

    fun getCompletedCount(): Int = completedCount

    fun getCommissionsPerHour(): Double {
        update()
        return cachedPerHour
    }

    fun getUptime(): String? {
        update()
        return cachedUptime
    }

    fun reset() {
        completedCount = 0
        startTime = null
        cachedPerHour = 0.0
        cachedUptime = null
        lastUpdate = null
    }
}

