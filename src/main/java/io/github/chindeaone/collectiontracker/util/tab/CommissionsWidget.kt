package io.github.chindeaone.collectiontracker.util.tab

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.util.ChatUtils


object CommissionsWidget {
    private var lastCommissionSet: List<String>? = null
    var rawCommissions: List<String> = emptyList()

    private var nextAllowedTime: Long = 0L
    private var firstInfoSeenTime: Long = 0L

    fun onTabWidgetsUpdate() {
        val areaWidget = TabWidget.AREA
        if (areaWidget.isPresent) {
            if (!areaWidget.lines.any { it.contains("Dwarven Mines") || it.contains("Crystal Hollows") || it.contains("Mineshaft") }) {
                // not in an area with commissions
                rawCommissions = emptyList()
                lastCommissionSet = null
                return
            }
        }

        val widget = TabWidget.COMMISSIONS
        val now = System.currentTimeMillis()

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
                return // Wait for the 1-second buffer
            }

            // disable the overlay if the widget is not found
            ChatUtils.sendMessage("§cWarning: Commissions widget not found. This can happen in low TPS lobbies. Please enable it using /widget or re-enable the commissions trackingOverlay config in your mod.", true)
            SkyblockCollectionTracker.configManager.config!!.mining.commissionsOverlay.enableCommissionsOverlay = false
            return
        }

        firstInfoSeenTime = 0L

        if (now < nextAllowedTime) return

        val currentRaw = parseCommissionWidget(widget.lines)
        if (currentRaw == null || currentRaw == lastCommissionSet) return

        rawCommissions = currentRaw
        lastCommissionSet = currentRaw
        nextAllowedTime = now + 3_000L // same as Hypixel

        rawCommissions.joinToString(" | ") { it }
    }

    private fun parseCommissionWidget(lines: List<String>): List<String>? {
        if (lines.size < 2) return null

        val body = lines.drop(1)
            .map { it.stripMinecraftFormatting().trim() }
            .filter { it.isNotEmpty() }
            .take(4)

        return body.ifEmpty { null }
    }

    private fun String.stripMinecraftFormatting(): String =
        replace(Regex("§."), "")
}