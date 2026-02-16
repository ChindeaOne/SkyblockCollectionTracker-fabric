package io.github.chindeaone.collectiontracker.utils

class TimerState {

    var endTimestamp = 0L
    var initialLag = 0L

    fun start(durationMs: Long) {
        endTimestamp = System.currentTimeMillis() + durationMs
        initialLag = ServerTickUtils.totalLagAdjustment
    }

    fun reset() {
        endTimestamp = 0L
        initialLag = 0L
    }

    val remainingSeconds: Double
        get() {
            if (endTimestamp == 0L) return 0.0
            val lagDelta = ServerTickUtils.totalLagAdjustment - initialLag
            val remaining = (endTimestamp + lagDelta) - System.currentTimeMillis()
            return (remaining / 1000.0).coerceAtLeast(0.0)
        }
}