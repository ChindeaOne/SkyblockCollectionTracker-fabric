package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MultiCollectionOverlay {

    public enum TrackingOptions {
        COLLECTION("Collection"),
        COLLECTION_RATE("Collection Rate"),
        COLLECTION_MADE("Collection Made"),
        MONEY_RATE("Money Rate"),
        MONEY_MADE("Money Made");

        private final String displayName;

        TrackingOptions(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum SummaryStats {
        COLLECTION,
        MONEY,
        BOTH
    }

    @Expose
    @ConfigOption(
            name = "Tracking Stats",
            desc = "Select what is displayed in the overlay for each collection."
    )
    @ConfigEditorDropdown
    public TrackingOptions trackingOptions = TrackingOptions.COLLECTION_RATE; // Default to collection amount

    @Expose
    @ConfigOption(
            name = "Tracking Summary",
            desc = "Shows a summary for all collections at the end of the tracking session."
    )
    @ConfigEditorBoolean
    public boolean multiTrackingSummary = true;

    @Expose
    @ConfigOption(
            name = "Detailed Gemstone Summary",
            desc = "Shows a more detailed summary for gemstones."
    )
    @ConfigEditorBoolean
    public boolean multiDetailedSummary = false;

    @Expose
    @ConfigOption(
            name = "Summary Stats",
            desc = "Choose what stats show in the summary."
    )
    @ConfigEditorDropdown
    public SummaryStats summaryStats = SummaryStats.BOTH; // Default to showing both collection and money

    @Expose
    @ConfigLink(owner = MultiCollectionOverlay.class, field = "multiCollectionOverlay")
    public Position multiOverlayPosition = new Position(50,100);
}
