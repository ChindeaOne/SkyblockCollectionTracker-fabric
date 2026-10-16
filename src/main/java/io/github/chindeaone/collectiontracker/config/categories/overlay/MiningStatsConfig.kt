package io.github.chindeaone.collectiontracker.config.categories.overlay

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MiningStatsConfig {
    @Expose
    @ConfigOption(
        name = "Mining Stats Overlay",
        desc = "Enable the mining stats overlay.\n§eNote: Your mining stats (e.g. Mining Speed, Mining Fortune, etc.) must be §bvisible§e in the Stats tab widget!"
    )
    @ConfigEditorBoolean
    var enableMiningStatsOverlay: Boolean = false

    @Expose
    @ConfigOption(
        name = "Mining Islands Only",
        desc = "Show the overlay only on Mining Islands."
    )
    @ConfigEditorBoolean
    var miningStatsOverlayInMiningIslandsOnly: Boolean = false

    @Expose
    @ConfigOption(
        name = "Detailed Fortune",
        desc = "Show the fortune breakdown in the overlay."
    )
    @ConfigEditorBoolean
    var showDetailedFortune: Boolean = false

    @Expose
    @ConfigLink(owner = MiningStatsConfig::class, field = "enableMiningStatsOverlay")
    var miningStatsOverlayPosition: Position = Position(50, 150)
}