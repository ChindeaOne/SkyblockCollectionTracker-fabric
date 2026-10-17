package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.fromNow
import kotlin.time.Duration
import kotlin.time.DurationUnit

class AbilityState {

    var endTimestamp: EpochMark? = null
    var initialLag: Duration = Duration.ZERO

    fun start(duration: Duration) {
        endTimestamp = duration.fromNow()
        initialLag = ServerTickUtils.totalLagAdjustment
    }

    fun reset() {
        endTimestamp = null
        initialLag = Duration.ZERO
    }

    val remainingDuration: Duration
        get() {
            val timestamp = endTimestamp ?: return Duration.ZERO
            val lagDelta = ServerTickUtils.totalLagAdjustment - initialLag
            val remaining = (timestamp + lagDelta).timeUntil()

            return remaining.coerceAtLeast(Duration.ZERO)
        }

    val remainingSeconds: Double
        get() = remainingDuration.toDouble(DurationUnit.SECONDS)
}