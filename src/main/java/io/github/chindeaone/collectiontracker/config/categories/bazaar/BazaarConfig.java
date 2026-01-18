package io.github.chindeaone.collectiontracker.config.categories.bazaar;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class BazaarConfig {

    public enum BazaarType {
        ENCHANTED_VERSION,
        SUPER_ENCHANTED_VERSION
    }

    public enum GemstoneVariant {
        ROUGH,
        FLAWED,
        FINE,
        FLAWLESS,
        PERFECT
    }

    @Expose
    @ConfigOption(
            name = "Use Bazaar Prices",
            desc = "Toggle to use bazaar prices instead of NPC prices."
    )
    @ConfigEditorBoolean
    public boolean useBazaar = false;

    @Expose
    @ConfigOption(
            name = "Select Bazaar Version",
            desc = "Select the version you want to use for pricing."
    )
    @ConfigEditorDropdown
    public BazaarType bazaarType = BazaarType.ENCHANTED_VERSION; // Default to ENCHANTED_VERSION

    @Expose
    @ConfigOption
            (name = "Select Gemstone Variant",
            desc = "Select the variant you want to use for pricing.")
    @ConfigEditorDropdown
    public GemstoneVariant gemstoneVariant = GemstoneVariant.FINE; // Default to FINE variant
}
