package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.chindeaone.collectiontracker.config.categories.overlay.*;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Tracking {

    @Expose
    @ConfigOption(name = "Collection Overlay", desc = "")
    @SerializedName("collectionConfig")
    @Accordion
    public CollectionConfig collectionConfig = new CollectionConfig();

    @Expose
    @ConfigOption(name = "Multi-Collection Overlay", desc = "")
    @SerializedName("multiCollectionConfig")
    @Accordion
    public MultiCollectionConfig multiCollectionConfig = new MultiCollectionConfig();

    @Expose
    @ConfigOption(name = "Skill Overlay", desc = "")
    @SerializedName("skillConfig")
    @Accordion
    public SkillConfig skillConfig = new SkillConfig();

    @Expose
    @ConfigOption(name = "Leaderboard Overlay", desc = "")
    @SerializedName("leaderboardConfig")
    @Accordion
    public LeaderboardConfig leaderboardConfig = new LeaderboardConfig();

    @Expose
    @ConfigOption(
            name = "API Tracking",
            desc = "Switch to tracking via Hypixel's API instead of sack messages.\n\n§eNote: Using the API will update the overlays slower, but it will count items in stash. Prefer this if you shaft or change lobbies a lot."
    )
    @SerializedName("apiTracking")
    @ConfigEditorBoolean
    public boolean apiTracking = false;

    @Expose
    @ConfigOption(
            name = "Custom Collection Color",
            desc = "Renders the collection overlay text using a custom color instead of the default one."
    )
    @SerializedName("overlayTextColor")
    @ConfigEditorBoolean
    public boolean overlayTextColor = false;

    @Expose
    @ConfigOption(
            name = "Explicit values",
            desc = "Show full values instead of formatted values for all overlays and summaries."
    )
    @SerializedName("explicitValues")
    @ConfigEditorBoolean
    public boolean explicitValues = false;
}