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
        desc = "Toggles an overlay for mining stats.\n§eYour mining stats (e.g. Mining Speed, Mining Fortune, etc.) must be §bvisible§e in the Stats widget for this to work."
    )
    @ConfigEditorBoolean
    var enableMiningStatsOverlay: Boolean = false

    @Expose
    @ConfigOption(name = "Mining Islands Only", desc = "Allows the overlay to be rendered only in Mining Islands.")
    @ConfigEditorBoolean
    var miningStatsOverlayInMiningIslandsOnly: Boolean = false

    @Expose
    @ConfigOption(name = "Show detailed fortune", desc = "Shows the fortune breakdown in the overlay.")
    @ConfigEditorBoolean
    var showDetailedFortune: Boolean = false

    @Expose
    @ConfigLink(owner = MiningStatsConfig::class, field = "enableMiningStatsOverlay")
    var miningStatsOverlayPosition: Position = Position(50, 150)
}