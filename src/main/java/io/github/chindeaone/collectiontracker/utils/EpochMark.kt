/**
 * Class implemented based on SkyHanni's SimpleTimeMark.
 */
package io.github.chindeaone.collectiontracker.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@JvmInline
value class EpochMark(val millis: Long): Comparable<EpochMark> {

    operator fun plus(duration: Duration): EpochMark = EpochMark(millis + duration.inWholeMilliseconds)

    operator fun minus(duration: Duration): EpochMark = EpochMark(millis - duration.inWholeMilliseconds)

    operator fun minus(other: EpochMark): Duration = (millis - other.millis).milliseconds

    fun timePassedSince(): Duration = now() - this
    fun timeUntil(): Duration = this - now()

    val isPassed: Boolean get() = millis <= nowMillis()
    val isInFuture: Boolean get() = millis > nowMillis()

    val keepPositive: Duration get() = timeUntil().coerceAtLeast(Duration.ZERO)

    override fun compareTo(other: EpochMark): Int = millis.compareTo(other.millis)

    companion object {
        private fun nowMillis(): Long = System.currentTimeMillis()

        fun now(): EpochMark = EpochMark(nowMillis())

        fun Duration.fromNow(): EpochMark = now() + this

        fun Duration.formatClock(): String = StringUtils.formatTime(inWholeSeconds)
        fun Duration.formatCompact(): String = StringUtils.formatCompactTime(inWholeSeconds)
        fun Duration.formatInWords(): String = StringUtils.formatTimeInWords(inWholeSeconds)
    }
}