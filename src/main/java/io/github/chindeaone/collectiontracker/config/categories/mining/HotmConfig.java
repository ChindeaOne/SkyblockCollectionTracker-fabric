package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class HotmConfig {

    @Expose
    @ConfigOption(
            name = "Professional mining speed",
            desc = "Input your mining speed from Professional perk."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 755, minStep = 1)
    public Property<Integer> professionalMS = Property.of(0);

    @Expose
    @ConfigOption(
            name = "Strong Arm mining speed",
            desc = "Input your mining speed from Strong Arm perk."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 505, minStep = 1)
    public Property<Integer> strongArmMS = Property.of(0);

    @Expose
    @ConfigOption(
            name = "Core Of The Mountain Level",
            desc = "Input your Core Of The Mountain level.\n§eRequired for more precise pickaxe ability cooldown."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 10, minStep = 1)
    public Property<Integer> cotmLevel = Property.of(0);

    @Expose
    @ConfigOption(
            name = "Better Sky Mall",
            desc = "Displays current Sky Mall perks and compacts Sky Mall chat messages"
    )
    @ConfigEditorBoolean
    public boolean enableSkyMall = false;

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
    @ConfigLink(owner = HotmConfig.class, field = "SkyMall")
    public Position skyMallPosition = new Position(500, 50);
}