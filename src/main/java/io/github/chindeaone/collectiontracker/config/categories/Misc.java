package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;
import io.github.notenoughupdates.moulconfig.observer.Property;

public class Misc {

    public enum TitleScale {
        SMALL(0.5f),
        MEDIUM(1.0f),
        LARGE(2.0f),
        HUGE(3.0f);

        private final float scale;

        TitleScale(float scale) {
            this.scale = scale;
        }

        public float getScale() {
            return scale;
        }
    }

    @Expose
    @ConfigOption(
            name = "Ability precision",
            desc = "Number of decimal shown for cooldown and duration."
    )
    @ConfigEditorSlider(minValue = 0, maxValue = 2, minStep = 1)
    public Property<Integer> abilityPrecision = Property.of(0);

    @Expose
    @ConfigOption(
            name = "Ability Title Duration",
            desc = "How long (in seconds) ability titles remain on screen."
    )
    @ConfigEditorSlider(minValue = 1, maxValue = 8, minStep = 1)
    public Property<Integer> abilityTitleDisplayTimer = Property.of(3);

    @Expose
    @ConfigOption(
            name = "Title Scale",
            desc = "Change the scale of the ability title.\n§eSmall = 0.5x, Medium = 1x, Large = 2x, Huge = 3x"
    )
    @ConfigEditorDropdown
    public TitleScale titleScale = TitleScale.MEDIUM; // Default to MEDIUM
}
