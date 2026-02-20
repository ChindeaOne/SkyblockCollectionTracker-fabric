package io.github.chindeaone.collectiontracker.config.categories.mining;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MiningRoutesConfig {

    public enum MineshaftSpawnRoutes {
        GEMSTONES("gemstone_spawn"),
        TUNGSTEN("tungsten_spawn"),
        MITHRIL("mithril_spawn");

        private final String type;

        MineshaftSpawnRoutes(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return name();
        }
    }

    @Expose
    @ConfigOption(
            name = "Enable Mineshaft Routes",
            desc = "Displays ordered waypoints for the Mineshaft type you enter.\n§eRoutes provided by Mining Cult."
    )
    @ConfigEditorBoolean
    public boolean enableMineshaftRoutes = false;

    @Expose
    @ConfigOption(
            name = "Enable routes for Mineshaft spawns",
            desc = "Enables routes for spawning Mineshafts."
    )
    @ConfigEditorBoolean
    public boolean enableMineshaftSpawnRoutes = false;

    @Expose
    @ConfigOption(
            name = "Mineshaft Spawn Routes",
            desc = "Select the route you want to use to spawn Mineshafts.\n§eRoutes provided by Mining Cult."
    )
    @ConfigEditorDropdown
    public MineshaftSpawnRoutes mineshaftSpawnRoutes = MineshaftSpawnRoutes.GEMSTONES;
}