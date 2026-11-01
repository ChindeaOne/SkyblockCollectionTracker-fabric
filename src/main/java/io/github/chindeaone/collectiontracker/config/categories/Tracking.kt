package io.github.chindeaone.collectiontracker.config.categories

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.collections.CollectionsManager.CollectionTier
import io.github.chindeaone.collectiontracker.collections.GemstonesManager.GemstoneTier
import io.github.chindeaone.collectiontracker.config.categories.overlay.CollectionConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.LeaderboardConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.MilestonesConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.MultiCollectionConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.SkillConfig
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Tracking {
    @Expose
    @ConfigOption(name = "Collection Overlay", desc = "")
    @Accordion
    var collectionConfig: CollectionConfig = CollectionConfig()

    @Expose
    @ConfigOption(name = "Multi-Collection Overlay", desc = "")
    @Accordion
    var multiCollectionConfig: MultiCollectionConfig = MultiCollectionConfig()

    @Expose
    @ConfigOption(name = "Skill Overlay", desc = "")
    @Accordion
    var skillConfig: SkillConfig = SkillConfig()

    @Expose
    @ConfigOption(name = "Leaderboard Overlay", desc = "")
    @Accordion
    var leaderboardConfig: LeaderboardConfig = LeaderboardConfig()

    @Expose
    @ConfigOption(name = "Milestones Overlay", desc = "")
    @Accordion
    var milestonesConfig: MilestonesConfig = MilestonesConfig()

    @Expose
    @ConfigOption(
        name = "API Tracking",
        desc = "Switch to tracking via Hypixel's API instead of sack messages.\n\n§eNote: Using the API will update the overlays slower, but it will count items in stash. Prefer this if you shaft or change lobbies a lot."
    )
    @ConfigEditorBoolean
    var apiTracking: Boolean = false

    @Expose
    @ConfigOption(
        name = "Format values",
        desc = "Show formatted values (e.g. 1M) instead of full values for all overlays and summaries."
    )
    @ConfigEditorBoolean
    var formattedValues: Boolean = true

    @Expose
    @ConfigOption(
        name = "Collection Format",
        desc = "Display collection rates, session amounts and summaries in the selected item format."
    )
    @ConfigEditorDropdown
    var collectionTier: CollectionTier = CollectionTier.BASE

    @Expose
    @ConfigOption(
        name = "Gemstone Format",
        desc = "Display gemstone rates, session amounts and summaries in the selected gemstone tier."
    )
    @ConfigEditorDropdown
    var collectionGemstoneTier: GemstoneTier = GemstoneTier.FINE

    @Expose
    @ConfigOption(
        name = "Custom Collection Color",
        desc = "Render the collection overlay text using a custom color instead of the default one."
    )
    @ConfigEditorBoolean
    var overlayTextColor: Boolean = false
}