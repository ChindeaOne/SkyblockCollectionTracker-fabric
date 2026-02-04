package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.chindeaone.collectiontracker.config.categories.overlay.CollectionOverlay;
import io.github.chindeaone.collectiontracker.config.categories.overlay.SkillOverlay;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Tracking {

    @Expose
    @ConfigOption(name = "Collection Overlay", desc = "")
    @SerializedName("collection_overlay")
    @Accordion
    public CollectionOverlay collectionOverlay = new CollectionOverlay();

    @Expose
    @ConfigOption(name = "Skills Tracking Overlay", desc = "")
    @SerializedName("skillOverlay")
    @Accordion
    public SkillOverlay skillOverlay = new SkillOverlay();

    @Expose
    @ConfigOption(
            name = "Explicit values",
            desc = "Show full values instead of rounded values for the overlays and summary."
    )
    @SerializedName("explicitValues")
    @ConfigEditorBoolean
    public boolean explicitValues = false;
}