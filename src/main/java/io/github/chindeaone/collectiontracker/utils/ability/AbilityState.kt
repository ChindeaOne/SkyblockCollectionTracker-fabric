package io.github.chindeaone.collectiontracker.utils.ability

import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.EpochMark.Companion.fromNow
import io.github.chindeaone.collectiontracker.utils.ServerTickUtils
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class AbilityState {

    var maxDuration: Duration = Duration.ZERO
        private set

    var maxCooldown: Duration = Duration.ZERO
        private set

    private var durationStartTimestamp: EpochMark? = null
    private var durationEndTimestamp: EpochMark? = null
    private var durationInitialLag: Duration = Duration.ZERO

    private var cooldownStartTimestamp: EpochMark? = null
    private var cooldownEndTimestamp: EpochMark? = null
    private var cooldownInitialLag: Duration = Duration.ZERO

    val remainingDuration: Duration
        get() {
            val timestamp = durationEndTimestamp ?: return Duration.ZERO
            val lagDelta = ServerTickUtils.totalLagAdjustment - durationInitialLag
            val remaining = (timestamp + lagDelta).timeUntil()

            return remaining.coerceAtLeast(Duration.ZERO)
        }

    val remainingCooldown: Duration
        get() {
            val timestamp = cooldownEndTimestamp ?: return Duration.ZERO
            val lagDelta = ServerTickUtils.totalLagAdjustment - cooldownInitialLag
            val remaining = (timestamp + lagDelta).timeUntil()

            return remaining.coerceAtLeast(Duration.ZERO)
        }

    val durationSeconds: Double
        get() = remainingDuration.toDouble(DurationUnit.SECONDS)

    val cooldownSeconds: Double
        get() = remainingCooldown.toDouble(DurationUnit.SECONDS)

    val isReady: Boolean
        get() = remainingCooldown <= Duration.ZERO && remainingDuration <= Duration.ZERO

    val isActive: Boolean
        get() = remainingDuration > Duration.ZERO

    fun start(duration: Duration, cooldown: Duration) {
        maxDuration = duration
        maxCooldown = cooldown

        val now = EpochMark.now()

        durationStartTimestamp = now
        durationEndTimestamp = duration.fromNow()
        durationInitialLag = ServerTickUtils.totalLagAdjustment

        cooldownStartTimestamp = now
        cooldownEndTimestamp = cooldown.fromNow()
        cooldownInitialLag = ServerTickUtils.totalLagAdjustment
    }

    fun updateDuration(newDuration: Duration) {
        val start = durationStartTimestamp ?: return
        maxDuration = newDuration
        durationEndTimestamp = start + newDuration
    }

    fun syncCooldown(timeSec: Long) {
        val currentRemaining = remainingCooldown
        val currentSec = currentRemaining.inWholeSeconds

        if (currentSec != timeSec) {
            val millisOffset = (currentRemaining.inWholeMilliseconds % 1000).milliseconds
            val newCooldown = timeSec.seconds + millisOffset
            cooldownEndTimestamp = newCooldown.fromNow()
            cooldownInitialLag = ServerTickUtils.totalLagAdjustment
        }
    }

    fun reset() {
        durationStartTimestamp = null
        durationEndTimestamp = null
        durationInitialLag = Duration.ZERO

        cooldownStartTimestamp = null
        cooldownEndTimestamp = null
        cooldownInitialLag = Duration.ZERO

        maxDuration = Duration.ZERO
        maxCooldown = Duration.ZERO
    }
}