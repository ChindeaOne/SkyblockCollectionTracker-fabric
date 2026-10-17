package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.config.serverLagProtection
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object ServerTickUtils {

    private var serverGameTime: Long = -1L
    private var packetSystemTime: EpochMark? = null
    var totalLagAdjustment: Duration = Duration.ZERO

    @JvmStatic
    fun onServerTick(gameTime: Long) {
        val previousGameTime = serverGameTime
        val previousSystemTime = packetSystemTime

        if (previousGameTime != -1L && previousSystemTime != null && serverLagProtection) {
            val expectedTicks = gameTime - previousGameTime
            val actualTime = previousSystemTime.timePassedSince()
            val expectedTime = (expectedTicks * 50).milliseconds

            totalLagAdjustment += actualTime - expectedTime
        }

        serverGameTime = gameTime
        packetSystemTime = EpochMark.now()
    }

    @JvmStatic
    fun reset() {
        serverGameTime = -1L
        packetSystemTime = null
        totalLagAdjustment = Duration.ZERO
    }
}