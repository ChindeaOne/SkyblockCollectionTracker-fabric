package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.categories.bazaar.BazaarConfig;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Bazaar {

    @Expose
    @ConfigOption(
            name = "Toggle Bazaar",
            desc = "Enable or disable Bazaar features."
    )
    @Accordion
    public BazaarConfig bazaarConfig = new BazaarConfig();
}
