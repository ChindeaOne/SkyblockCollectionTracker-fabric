package io.github.chindeaone.collectiontracker.config.categories

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.categories.farmingweight.FarmingweightConfig
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Farming {
    @Expose
    @ConfigOption(name = "Farming Weight", desc = "")
    @Accordion
    var farmingweightConfig: FarmingweightConfig = FarmingweightConfig()
}
