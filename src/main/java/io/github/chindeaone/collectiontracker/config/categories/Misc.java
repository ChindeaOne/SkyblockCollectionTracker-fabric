package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class Misc {

    @Expose
    @ConfigOption(
            name = "Ability precision",
            desc = "Number of decimal shown for cooldown and duration"
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 2, minStep = 1)
    public Property<Integer> abilityPrecision = Property.of(0);
}
