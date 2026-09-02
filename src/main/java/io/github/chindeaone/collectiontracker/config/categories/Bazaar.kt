package io.github.chindeaone.collectiontracker.config.categories

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Bazaar {
    enum class BazaarPriceType {
        INSTANT_SELL,
        INSTANT_BUY
    }

    enum class BazaarType {
        ENCHANTED_VERSION,
        SUPER_ENCHANTED_VERSION
    }

    enum class GemstoneVariant {
        ROUGH,
        FLAWED,
        FINE,
        FLAWLESS,
        PERFECT
    }

    @Expose
    @ConfigOption(name = "Bazaar Prices", desc = "Toggle to use bazaar prices instead of NPC prices.")
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
    @ConfigOption(name = "Bazaar Version", desc = "Select the version you want to use for pricing.")
    @ConfigEditorDropdown
    var bazaarType: BazaarType = BazaarType.ENCHANTED_VERSION // Default to ENCHANTED_VERSION

    @Expose
    @ConfigOption(name = "Gemstone Variant", desc = "Select the variant you want to use for pricing.")
    @ConfigEditorDropdown
    var gemstoneVariant: GemstoneVariant = GemstoneVariant.FINE // Default to FINE variant
}
