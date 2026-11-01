package io.github.chindeaone.collectiontracker.config.categories

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.collections.GemstonesManager.GemstoneTier
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarPriceType
import io.github.chindeaone.collectiontracker.collections.prices.BazaarPrices.BazaarType
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Bazaar {

    @Expose
    @ConfigOption(
        name = "Bazaar Prices",
        desc = "Toggle to use bazaar prices instead of NPC prices."
    )
    @ConfigEditorBoolean
    var useBazaar: Boolean = false

    @Expose
    @ConfigOption(
        name = "Bazaar Price Type",
        desc = "Select whether to use Instant Sell or Instant Buy prices from the Bazaar."
    )
    @ConfigEditorDropdown
    var bazaarPriceType: BazaarPriceType = BazaarPriceType.INSTANT_BUY // Default to INSTANT_BUY

    @Expose
    @ConfigOption(
        name = "Bazaar Version",
        desc = "Select the version you want to use for pricing."
    )
    @ConfigEditorDropdown
    var bazaarType: BazaarType = BazaarType.ENCHANTED_VERSION // Default to ENCHANTED_VERSION

    @Expose
    @ConfigOption(
        name = "Gemstone Variant",
        desc = "Select the variant you want to use for pricing."
    )
    @ConfigEditorDropdown
    var gemstoneTier: GemstoneTier = GemstoneTier.FINE // Default to FINE
}
