package io.github.chindeaone.collectiontracker.config.categories.mining.routes;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class PureOresRoutes {

    public enum PureOreRoutes {
        DIAMOND("diamond"),
        EMERALD("emerald"),
        IRON("iron"),
        LAPIS("lapis"),
        REDSTONE("redstone"),
        QUARTZ("quartz");

        private final String type;

        PureOreRoutes(String type) {
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
            name = "Enable Pure Ores Routes",
            desc = "Enables routes for Pure Ores."
    )
    @ConfigEditorBoolean
    public boolean enablePureOresRoutes = false;

    @Expose
    @ConfigOption(
            name = "Pure Ores Route",
            desc = "Select the route you want to use for Pure Ores.\n§eRoutes provided by Mining Cult."
    )
    @ConfigEditorDropdown
    public PureOreRoutes selectedPureOresRoute = PureOreRoutes.DIAMOND;
}