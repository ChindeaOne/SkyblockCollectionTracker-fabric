package io.github.chindeaone.collectiontracker.config.categories.mining.routes;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class DwarvenMetalsRoutes {

    public enum DwarvenMetalRoutes {
        PURE_UMBER("pure_umber"),
        HYBRID_METALS("hybrid_metals"),;

        private final String type;

        DwarvenMetalRoutes(String type) {
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
            name = "Enable Dwarven Metal Routes",
            desc = "Enables routes for Dwarven Metals."
    )
    @ConfigEditorBoolean
    public boolean enableDwarvenMetalRoutes = false;

    @Expose
    @ConfigOption(
            name = "Dwarven Metal Route",
            desc = "Select the route you want to use for Dwarven Metals.\n§eRoutes provided by Mining Cult."
    )
    @ConfigEditorDropdown
    public DwarvenMetalRoutes selectedDwarvenMetalRoute = DwarvenMetalRoutes.PURE_UMBER;
}
