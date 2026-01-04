package io.github.chindeaone.collectiontracker.util.tab

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.util.ChatUtils


object CommissionsWidget {
    private var lastCommissionSet: List<String>? = null
    var rawCommissions: List<String> = emptyList()

    private var nextAllowedTime: Long = 0L

    fun onTabWidgetsUpdate() {
        val widget = TabWidget.COMMISSIONS
        if (!widget.isPresent) {
            if (!TabWidget.INFO.isPresent) return // avoid spamming messages when tab widgets are not visible

            // disable the overlay if the widget is not found
            ChatUtils.sendMessage("§cCommissions widget not found. Please enable it using /widget.", true)
            SkyblockCollectionTracker.configManager.config!!.mining.commissionsOverlay.enableCommissionsOverlay = false
            return
        }

        val now = System.currentTimeMillis()
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