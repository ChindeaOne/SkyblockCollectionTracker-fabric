package io.github.chindeaone.collectiontracker.config.categories.foraging;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BeekeeperConfig {

    @Expose
    @ConfigOption(
            name = "Better Beekeeper",
            desc = "Displays current Beekeeper perks and compacts Beekeeper chat messages."
    )
    @ConfigEditorBoolean
    public boolean enableBeekeeper = false;

    @Expose
    public String lastBeekeeperBuff = "";

    @Expose
    @ConfigOption(
            name = "Foraging Islands Only",
            desc = "Allows the Beekeeper overlay to be rendered only in Foraging Islands."
    )
    @ConfigEditorBoolean
    public boolean beekeeperInForagingIslandsOnly = true;

    @Expose
    @ConfigOption(
            name = "Disable Beekeeper chat messages",
            desc = "Hides Beekeeper chat messages while displaying perks in the overlay and allowing other mods to process the messages."
    )
    @ConfigEditorBoolean
    public boolean disableBeekeeperChatMessages = false;

    @Expose
    @ConfigLink(owner = BeekeeperConfig.class, field = "enableBeekeeper")
    public Position beekeeperPosition = new Position(500, 150);
}
