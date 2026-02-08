package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class HotmPerks {

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
}