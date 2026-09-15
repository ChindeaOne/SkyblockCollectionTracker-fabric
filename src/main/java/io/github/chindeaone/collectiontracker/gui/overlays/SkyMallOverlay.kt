package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.enableSkyMall
import io.github.chindeaone.collectiontracker.config.skyMallInMiningIslandsOnly
import io.github.chindeaone.collectiontracker.config.skyMallPosition
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.currentSkyMallBuff
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker.currentMiningIsland
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping.miningIslands

class SkyMallOverlay : AbstractRotatingPerksOverlay() {
    override val overlayLabel: String = "Sky Mall"

    override val position: Position get() = skyMallPosition

    override val isEnabled: Boolean get() = enableSkyMall

    override val buffPrefix get() = "§bSky Mall"

    override val currentBuff: String get() = currentSkyMallBuff

    override val isIslandAllowed: Boolean get() = !skyMallInMiningIslandsOnly || miningIslands.contains(currentMiningIsland)
}