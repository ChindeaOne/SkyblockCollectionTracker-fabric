package io.github.chindeaone.collectiontracker.config.categories

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.github.chindeaone.collectiontracker.config.categories.overlay.CollectionConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.LeaderboardConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.MultiCollectionConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.SkillConfig
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Tracking {
    @Expose
    @ConfigOption(name = "Collection Overlay", desc = "")
    @SerializedName("collectionConfig")
    @Accordion
    var collectionConfig: CollectionConfig = CollectionConfig()

    @Expose
    @ConfigOption(name = "Multi-Collection Overlay", desc = "")
    @SerializedName("multiCollectionConfig")
    @Accordion
    var multiCollectionConfig: MultiCollectionConfig = MultiCollectionConfig()

    @Expose
    @ConfigOption(name = "Skill Overlay", desc = "")
    @SerializedName("skillConfig")
    @Accordion
    var skillConfig: SkillConfig = SkillConfig()

    @Expose
    @ConfigOption(name = "Leaderboard Overlay", desc = "")
    @SerializedName("leaderboardConfig")
    @Accordion
    var leaderboardConfig: LeaderboardConfig = LeaderboardConfig()

    @Expose
    @ConfigOption(
        name = "API Tracking",
        desc = "Switch to tracking via Hypixel's API instead of sack messages.\n\n§eNote: Using the API will update the overlays slower, but it will count items in stash. Prefer this if you shaft or change lobbies a lot."
    )
    @SerializedName("apiTracking")
    @ConfigEditorBoolean
    var apiTracking: Boolean = false

    @Expose
    @ConfigOption(
        name = "Custom Collection Color",
        desc = "Renders the collection overlay text using a custom color instead of the default one."
    )
    @SerializedName("overlayTextColor")
    @ConfigEditorBoolean
    var overlayTextColor: Boolean = false

    @Expose
    @ConfigOption(
        name = "Explicit values",
        desc = "Show full values instead of formatted values for all overlays and summaries."
    )
    @SerializedName("explicitValues")
    @ConfigEditorBoolean
    var explicitValues: Boolean = false
}