package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getLotteryPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isLotteryEnabled
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isLotteryInForagingIslandsOnly
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.currentLotteryBuff
import io.github.chindeaone.collectiontracker.utils.world.ForagingMapping.foragingIslands
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker.currentForagingIsland

class LotteryOverlay : AbstractRotatingPerksOverlay() {
    override val overlayLabel: String = "Lottery"

    override val position: Position get() = getLotteryPosition()

    override val isEnabled: Boolean get() = isLotteryEnabled()

    override val buffPrefix get() = "§2Lottery"

    override val currentBuff: String get() = currentLotteryBuff

    override val isIslandAllowed: Boolean get() = !isLotteryInForagingIslandsOnly() || foragingIslands.contains(currentForagingIsland)
}
