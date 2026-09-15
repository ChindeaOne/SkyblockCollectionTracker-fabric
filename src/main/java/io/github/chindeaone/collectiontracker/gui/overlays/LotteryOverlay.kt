package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.enableLottery
import io.github.chindeaone.collectiontracker.config.lotteryInForagingIslandsOnly
import io.github.chindeaone.collectiontracker.config.lotteryPosition
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.currentLotteryBuff
import io.github.chindeaone.collectiontracker.utils.world.ForagingMapping.foragingIslands
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker.currentForagingIsland

class LotteryOverlay : AbstractRotatingPerksOverlay() {
    override val overlayLabel: String = "Lottery"

    override val position: Position get() = lotteryPosition

    override val isEnabled: Boolean get() = enableLottery

    override val buffPrefix get() = "§2Lottery"

    override val currentBuff: String get() = currentLotteryBuff

    override val isIslandAllowed: Boolean get() = !lotteryInForagingIslandsOnly || foragingIslands.contains(currentForagingIsland)
}
