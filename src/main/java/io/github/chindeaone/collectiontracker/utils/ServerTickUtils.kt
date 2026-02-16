package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.config.ConfigAccess

object ServerTickUtils {

    private var lastServerGameTime = -1L
    private var lastPacketSystemTime = -1L
    var totalLagAdjustment = 0L
        private set

    @JvmStatic
    fun onServerTick(gameTime: Long) {
        val now = System.currentTimeMillis()

        if (lastServerGameTime != -1L && ConfigAccess.isServerLagProtectionEnabled()) {
            val expectedTicks = gameTime - lastServerGameTime
            val actualTime = now - lastPacketSystemTime
            val expectedTime = expectedTicks * 50
            totalLagAdjustment += (actualTime - expectedTime)
        }

        lastServerGameTime = gameTime
        lastPacketSystemTime = now
    }

    @JvmStatic
    fun reset() {
        lastServerGameTime = -1L
        lastPacketSystemTime = -1L
        totalLagAdjustment = 0L
    }
}