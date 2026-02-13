package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class SkyMallConfig {

    @Expose
    @ConfigOption(
            name = "Better Sky Mall",
            desc = "Displays current Sky Mall perks and compacts Sky Mall chat messages."
    )
    @ConfigEditorBoolean
    public boolean enableSkyMall = false;

    @Expose
    public String lastSkyMallPerk = "";

    @Expose
    @ConfigOption(
            name = "Mining Islands Only",
            desc = "Allows the Sky Mall overlay to be rendered only in Mining Islands."
    )
    @ConfigEditorBoolean
    public boolean skyMallInMiningIslandsOnly = true;

    @Expose
    @ConfigOption(
            name = "Disable Sky Mall chat messages",
            desc = "Hides Sky Mall chat messages while displaying perks in the overlay and allowing other mods to process the messages."
    )
    @ConfigEditorBoolean
    public boolean disableSkyMallChatMessages = false;

    @Expose
    @ConfigLink(owner = SkyMallConfig.class, field = "SkyMall")
    public Position skyMallPosition = new Position(500, 50);
}