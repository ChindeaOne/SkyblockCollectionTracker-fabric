package io.github.chindeaone.collectiontracker.utils.tab

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.utils.Colors
import io.github.chindeaone.collectiontracker.utils.parser.CommissionParser
import io.github.chindeaone.collectiontracker.utils.parser.CommissionParser.ActiveCommission
import io.github.chindeaone.collectiontracker.utils.parser.ContainerParser
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import net.minecraft.network.chat.Component

object CommissionWidget {
    var commissions: MutableList<ActiveCommission> = mutableListOf()
    private var ignoredStates = mutableListOf<List<ActiveCommission>>()

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
        if (!ConfigAccess.isNewCommissionTitleEnabled()) return

        val color = updated.type.color ?: Colors.YELLOW.color

        val component = Component.empty()
            .append(Component.literal("New Commission: ").withColor(Colors.YELLOW.color))
            .append(Component.literal(updated.type.name).withColor(color))

        RenderUtils.showTitle(component, 1500)
    }

    fun completeCollectorCommission(commissionName: String) {
        val active = commissions.firstOrNull {
            it.type.name.equals(commissionName, ignoreCase = true)
        } ?: return

        active.progress = "DONE"

        if (!ConfigAccess.isCompletionTitleEnabled()) return

        val color = active.type.color ?: Colors.GREEN.color

        val component = Component.empty()
            .append(Component.literal(active.type.name).withColor(color))
            .append(Component.literal(" Completed!").withColor(Colors.GREEN.color))

        RenderUtils.showTitle(component, 1500)
    }

    fun onTabUpdate() {
        if (!ConfigAccess.isCommissionsOverlayEnabled()) {
            commissions.clear()
            return
        }

        if (ContainerParser.isCommissionMenuOpen) {
            return
        }

        if (!IslandTracker.currentMiningIsland.let { it.equals("Dwarven Mines") || it.equals("Crystal Hollows") || it.equals("Mineshaft") }) {
            // not in an area with commissions
            commissions.clear()
            return
        }

        val widget = TabWidget.COMMISSIONS

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