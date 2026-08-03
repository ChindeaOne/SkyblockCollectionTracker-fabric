package io.github.chindeaone.collectiontracker.utils.tab

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.ColorUtils
import io.github.chindeaone.collectiontracker.utils.CommissionUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import io.github.chindeaone.collectiontracker.utils.parser.CommissionParser
import io.github.chindeaone.collectiontracker.utils.parser.CommissionParser.ActiveCommission
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import net.minecraft.network.chat.Component

object CommissionWidget {
    @JvmStatic
    var commissions: MutableList<ActiveCommission> = mutableListOf()
        private set
    private var ignoredStates = mutableListOf<List<ActiveCommission>>()

    private var firstInfoSeenTime: Long = 0L

    fun updateCommission(index: Int, newValue: String) {
        val updated = CommissionParser.parseCommission(newValue) ?: return
        if (commissions.getOrNull(index) == updated) return
        if (index !in commissions.indices) return

        val current = commissions.getOrNull(index)
        if (current != null && current.completed && updated.isFresh && current.type != updated.type) {
            ignoredStates.add(commissions.map { it.copy() })
        }

        commissions[index] = updated

        if (!updated.isFresh) return
        if (!ConfigAccess.isClaimTitleEnabled()) return

        val color = updated.type.color ?: ColorUtils.YELLOW

        val component = Component.empty()
            .append(Component.literal("New Commission: ").withColor(ColorUtils.YELLOW))
            .append(Component.literal(updated.type.name).withColor(color))

        RenderUtils.showTitle(component, 1500)
    }

    fun completeCollectorCommission(commissionName: String) {
        val active = commissions.firstOrNull {
            it.type.name.equals(commissionName, ignoreCase = true)
        } ?: return

        active.progress = "DONE"

        if (!ConfigAccess.isCompletionTitleEnabled()) return

        val color = active.type.color ?: ColorUtils.GREEN

        val component = Component.empty()
            .append(Component.literal(active.type.name).withColor(color))
            .append(Component.literal(" Completed!").withColor(ColorUtils.GREEN))

        RenderUtils.showTitle(component, 1500)
    }

    fun onTabWidgetsUpdate() {
        if (!ConfigAccess.isCommissionsEnabled()) {
            commissions.clear()
            return
        }

        if (CommissionUtils.isMenuOpen) {
            return
        }

        if (!IslandTracker.currentMiningIsland.let { it.equals("Dwarven Mines") || it.equals("Crystal Hollows") || it.equals("Mineshaft") }) {
            // not in an area with commissions
            commissions.clear()
            return
        }

        val widget = TabWidget.COMMISSIONS

        if (!widget.isPresent) {
            // avoid spamming messages when tab widgets are not visible
            if (!TabWidget.INFO.isPresent) {
                firstInfoSeenTime = 0L
                return
            }
            val now = System.currentTimeMillis()

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

        val currentRaw = TabData.parseWidgetData(widget.lines) ?: return

        val parsed = currentRaw.mapNotNull(CommissionParser::parseCommission)
        if (parsed == commissions) return

        val ignored = ignoredStates.firstOrNull { it == parsed }
        if (ignored != null) {
            ignoredStates.remove(ignored) // tab updated not by the commission widget -> don't update
            return
        }

        commissions = parsed.toMutableList()
    }
}