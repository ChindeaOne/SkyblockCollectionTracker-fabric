package io.github.chindeaone.collectiontracker.config.categories.mining.routes;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MiningRoutesConfig {

    @Expose
    @ConfigOption(name = "Mineshaft Routes", desc = "")
    @Accordion
    public MineshaftRoutes mineshaftRoutes = new MineshaftRoutes();

    @Expose
    @ConfigOption(name = "Dwarven Metal Routes", desc = "")
    @Accordion
    public DwarvenMetalsRoutes dwarvenMetalsRoutes = new DwarvenMetalsRoutes();

    @Expose
    @ConfigOption(name = "Pure Ores Routes", desc = "")
    @Accordion
    public PureOresRoutes pureOresRoutes = new PureOresRoutes();
}