package io.github.chindeaone.collectiontracker.config.categories.mining.routes

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class DwarvenMetalsRoutes {
    @Suppress("unused")
    enum class DwarvenMetalRoutes(val type: String) {
        PURE_UMBER("pure_umber"),
        HYBRID_METALS("hybrid_metals");

        override fun toString(): String {
            return name
        }
    }

    @Expose
    @ConfigOption(name = "Enable Dwarven Metal Routes", desc = "Enables routes for Dwarven Metals.")
    @ConfigEditorBoolean
    var enableDwarvenMetalRoutes: Boolean = false

    @Expose
    @ConfigOption(
        name = "Dwarven Metal Route",
        desc = "Select the route you want to use for Dwarven Metals.\n§eRoutes provided by Mining Cult."
    )
    @ConfigEditorDropdown
    var selectedDwarvenMetalRoute: DwarvenMetalRoutes = DwarvenMetalRoutes.PURE_UMBER
}
