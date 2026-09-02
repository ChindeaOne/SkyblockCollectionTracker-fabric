package io.github.chindeaone.collectiontracker.config.categories.overlay

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ForagingStatsConfig {
    @Expose
    @ConfigOption(
        name = "Foraging Stats Overlay",
        desc = "Toggles an overlay for foraging stats.\n§eYour foraging stats stats (e.g. Foraging Fortune, Sweep etc.) must be §bvisible§e in the Stats widget for this to work."
    )
    @ConfigEditorBoolean
    var enableForagingStatsOverlay: Boolean = false

    @Expose
    @ConfigOption(name = "Foraging Islands Only", desc = "Allows the overlay to be rendered only in Foraging Islands.")
    @ConfigEditorBoolean
    var foragingStatsOverlayInForagingIslandsOnly: Boolean = false

    @Expose
    @ConfigOption(name = "Show detailed fortune", desc = "Shows the fortune breakdown in the overlay.")
    @ConfigEditorBoolean
    var showDetailedFortune: Boolean = false

    @Expose
    @ConfigLink(owner = ForagingStatsConfig::class, field = "enableForagingStatsOverlay")
    var foragingStatsOverlayPosition: Position = Position(50, 200)
}
