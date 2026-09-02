package io.github.chindeaone.collectiontracker.config.categories.overlay

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MultiCollectionConfig {
    enum class TrackingOptions(private val displayName: String) {
        COLLECTION("Collection"),
        COLLECTION_RATE("Collection Rate"),
        COLLECTION_MADE("Collection Made"),
        MONEY_RATE("Money Rate"),
        MONEY_MADE("Money Made");

        override fun toString(): String {
            return displayName
        }
    }

    @Suppress("unused")
    enum class SummaryStats {
        COLLECTION,
        MONEY,
        BOTH
    }

    @Expose
    @ConfigOption(name = "Tracking Stats", desc = "Select what is displayed in the overlay for each collection.")
    @ConfigEditorDropdown
    var trackingOptions: TrackingOptions = TrackingOptions.COLLECTION_RATE // Default to collection amount

    @Expose
    @ConfigOption(
        name = "Tracking Summary",
        desc = "Shows a summary for all collections at the end of the tracking session."
    )
    @ConfigEditorBoolean
    var multiTrackingSummary: Boolean = true

    @Expose
    @ConfigOption(name = "Detailed Gemstone Summary", desc = "Shows a more detailed summary for gemstones.")
    @ConfigEditorBoolean
    var multiDetailedSummary: Boolean = false

    @Expose
    @ConfigOption(name = "Summary Stats", desc = "Choose what stats show in the summary.")
    @ConfigEditorDropdown
    var summaryStats: SummaryStats = SummaryStats.BOTH // Default to showing both collection and money

    @Expose
    @ConfigLink(owner = MultiCollectionConfig::class, field = "trackingOptions")
    var multiOverlayPosition: Position = Position(50, 100)
}
