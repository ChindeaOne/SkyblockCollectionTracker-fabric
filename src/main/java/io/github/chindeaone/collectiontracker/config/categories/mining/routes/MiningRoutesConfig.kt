package io.github.chindeaone.collectiontracker.config.categories.mining.routes

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MiningRoutesConfig {
    @Expose
    @ConfigOption(name = "Mineshaft Routes", desc = "")
    @Accordion
    var mineshaftRoutes: MineshaftRoutes = MineshaftRoutes()

    @Expose
    @ConfigOption(name = "Dwarven Metal Routes", desc = "")
    @Accordion
    var dwarvenMetalsRoutes: DwarvenMetalsRoutes = DwarvenMetalsRoutes()

    @Expose
    @ConfigOption(name = "Pure Ores Routes", desc = "")
    @Accordion
    var pureOresRoutes: PureOresRoutes = PureOresRoutes()
}