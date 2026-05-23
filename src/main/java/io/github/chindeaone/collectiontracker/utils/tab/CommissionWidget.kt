package io.github.chindeaone.collectiontracker.utils.tab

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker

object CommissionWidget {
    private var lastCommissionSet: List<String>? = null
    var rawCommissions: MutableList<String> = mutableListOf()

    private var nextAllowedTime: Long = 0L
    private var firstInfoSeenTime: Long = 0L

    fun updateCommission(index: Int, newValue: String) {
        if (index < 0) return

        // Ensure the list is large enough
        while (rawCommissions.size <= index) {
            rawCommissions.add("")
        }

        rawCommissions[index] = newValue
        lastCommissionSet = ArrayList(rawCommissions)
        nextAllowedTime = System.currentTimeMillis() + 3000L // lock for 3s to allow tab data to update
    }

    fun onTabWidgetsUpdate() {
        if (!ConfigAccess.isCommissionsEnabled()) {
            rawCommissions = mutableListOf()
            lastCommissionSet = null
            return
        }

        val now = System.currentTimeMillis()
        if (now < nextAllowedTime) return

        if (!IslandTracker.currentMiningIsland.let { it.equals("Dwarven Mines") || it.equals("Crystal Hollows") || it.equals("Mineshaft") }) {
                // not in an area with commissions
                rawCommissions = mutableListOf()
                lastCommissionSet = null
                return
        }

        val widget = TabWidget.COMMISSIONS

        if (!widget.isPresent) {
            // avoid spamming messages when tab widgets are not visible
            if (!TabWidget.INFO.isPresent) {
                firstInfoSeenTime = 0L
                return
            }

            if (firstInfoSeenTime == 0L) {
                firstInfoSeenTime = now
            }

            if (now - firstInfoSeenTime < 5_000L) {
                return // Wait for the 5s buffer
            }

            // disable the overlay if the widget is not found
            ChatUtils.sendMessage("§cWarning: Commissions widget not found. This can happen in low TPS lobbies. Please enable it using /widget or re-enable the commissions overlay config in your mod.", true)
            ConfigHelper.disableCommissions()
            return
        }

        firstInfoSeenTime = 0L

        val currentRaw = TabData.parseWidgetData(widget.lines)
        if (currentRaw == null || currentRaw == lastCommissionSet) return

        rawCommissions = currentRaw.toMutableList()
        lastCommissionSet = currentRaw
        nextAllowedTime = now + 3_000L // same as Hypixel

        rawCommissions.joinToString(" | ") { it }
    }
}