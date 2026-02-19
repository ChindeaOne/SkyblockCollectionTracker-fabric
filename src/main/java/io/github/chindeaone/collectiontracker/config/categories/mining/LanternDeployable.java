package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class LanternDeployable {

    @Expose
    @ConfigOption(
            name = "Enable Lantern Deployable",
            desc = "Displays name and timer for the deployable."
    )
    @ConfigEditorBoolean
    public boolean enableDeployable = false;

    @Expose
    @ConfigOption(
            name = "Show Title",
            desc = "Shows a title when it expires."
    )
    @ConfigEditorBoolean
    public boolean showDeployableTitle = false;

    @Expose
    @ConfigLink(owner = LanternDeployable.class, field = "Lantern Deployable")
    public Position deployablePosition = new Position(300, 100);
}
