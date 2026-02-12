package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.*;
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
    @ConfigOption(name = "Pickaxe Ability Config", desc = "")
    @Accordion
    public PickaxeAbilityConfig pickaxeAbilityConfig = new PickaxeAbilityConfig();

    @Expose
    @ConfigOption(name = "Sky Mall Config", desc = "")
    @Accordion
    public SkyMallConfig skyMallConfig = new SkyMallConfig();
}