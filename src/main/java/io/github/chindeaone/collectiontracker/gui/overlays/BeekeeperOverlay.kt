package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getBeekeeperPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isBeekeeperEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isBeekeeperInForagingIslandsOnly
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.currentBeekeeperBuff
import io.github.chindeaone.collectiontracker.utils.world.ForagingMapping.foragingIslands
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker.currentForagingIsland

class BeekeeperOverlay : AbstractRotatingPerksOverlay() {
    override val overlayLabel: String = "Beekeeper"

    override val position: Position get() = getBeekeeperPosition()

    override val isEnabled: Boolean get() = isBeekeeperEnabled()

    override val buffPrefix get() = "§6Beekeeper"

    override val currentBuff: String get() = currentBeekeeperBuff

    override val isIslandAllowed: Boolean get() = !isBeekeeperInForagingIslandsOnly() || foragingIslands.contains(currentForagingIsland)
}
