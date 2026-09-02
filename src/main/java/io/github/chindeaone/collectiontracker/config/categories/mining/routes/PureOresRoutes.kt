package io.github.chindeaone.collectiontracker.config.categories.mining.routes

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class PureOresRoutes {
    @Suppress("unused")
    enum class PureOreRoutes(val type: String) {
        DIAMOND("diamond"),
        EMERALD("emerald"),
        IRON("iron"),
        LAPIS("lapis"),
        REDSTONE("redstone"),
        QUARTZ("quartz");

        override fun toString(): String {
            return name
        }
    }

    @Expose
    @ConfigOption(name = "Enable Pure Ores Routes", desc = "Enables routes for Pure Ores.")
    @ConfigEditorBoolean
    var enablePureOresRoutes: Boolean = false

    @Expose
    @ConfigOption(
        name = "Pure Ores Route",
        desc = "Select the route you want to use for Pure Ores.\n§eRoutes provided by Mining Cult."
    )
    @ConfigEditorDropdown
    var selectedPureOresRoute: PureOreRoutes = PureOreRoutes.DIAMOND
}