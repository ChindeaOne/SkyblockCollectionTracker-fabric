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
    @ConfigOption(
        name = "Pure Ores Routes",
        desc = "Enable routes for Pure Ores in §2Dwarven Mines§f."
    )
    @ConfigEditorBoolean
    var enablePureOresRoutes: Boolean = false

    @Expose
    @ConfigOption(name = "Select Route", desc = "")
    @ConfigEditorDropdown
    var selectedPureOresRoute: PureOreRoutes = PureOreRoutes.DIAMOND
}