package io.github.chindeaone.collectiontracker.utils

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object SkyblockTimeUtils {

    private const val SKYBLOCK_TEN_MINUTES = 25.0 / 3.0 // 8.333 seconds per 10 SkyBlock minutes

    fun Int.toRealSeconds(): Duration = (this / 10.0 * SKYBLOCK_TEN_MINUTES).seconds

    fun skyblockTimeInMinutes(hour: Int, minute: Int): Int = hour * 60 + minute
}