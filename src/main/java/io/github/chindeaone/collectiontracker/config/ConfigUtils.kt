package io.github.chindeaone.collectiontracker.config

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.categories.About
import io.github.chindeaone.collectiontracker.config.categories.Bazaar
import io.github.chindeaone.collectiontracker.config.categories.Foraging
import io.github.chindeaone.collectiontracker.config.categories.Mining
import io.github.chindeaone.collectiontracker.config.categories.Tracking
import io.github.chindeaone.collectiontracker.config.categories.coleweight.Coleweight
import io.github.chindeaone.collectiontracker.config.categories.mining.HotmPerks
import io.github.chindeaone.collectiontracker.config.categories.mining.KeybindConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.CollectionOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.CommissionsOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.ForagingStatsOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.MiningStatsOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.SkillOverlay
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.observer.Property

/**
 * Global accessors for the configuration.
 */
val modConfig: ModConfig get() = SkyblockCollectionTracker.configManager.config!!

// Position Config Accessor
val trackingPosition: Position get() = modConfig.trackingOverlay.collectionOverlay.overlayPosition
val miningStatsPosition: Position get() = modConfig.mining.miningStatsOverlay.miningStatsOverlayPosition
val foragingStatsPosition: Position get() = modConfig.foraging.foragingStatsOverlay.foragingStatsOverlayPosition
val commissionsPosition: Position get() = modConfig.mining.commissionsOverlay.commissionsOverlayPosition
val skillPosition: Position get() = modConfig.trackingOverlay.skillOverlay.skillOverlayPosition

// About Config Accessor
val aboutConfig: About get() = modConfig.about
val updateType: About.UpdateType get() = aboutConfig.update
val hasCheckedUpdate: Boolean get() = aboutConfig.hasCheckedUpdate

// Bazaar Config Accessors
val bazaarConfig: Bazaar get() = modConfig.bazaar
val bazaarPriceType: Bazaar.BazaarPriceType get() = bazaarConfig.bazaarPriceType
val useBazaar: Boolean get() = bazaarConfig.useBazaar
val bazaarType: Bazaar.BazaarType get() = bazaarConfig.bazaarType
val gemstoneVariant: Bazaar.GemstoneVariant get() = bazaarConfig.gemstoneVariant

// Mining Config Accessors
val miningConfig: Mining get() = modConfig.mining
val commissionsOverlay: CommissionsOverlay get() = miningConfig.commissionsOverlay
val keybindConfig: KeybindConfig get() = commissionsOverlay.commissions
val coleweightConfig: Coleweight get() = miningConfig.coleweight
val hotmConfig: HotmPerks get() = miningStatsOverlay.hotmPerks
// Overlays
val miningStatsOverlay: MiningStatsOverlay get() = miningConfig.miningStatsOverlay
val enableMiningStatsOverlay: Boolean get() = miningStatsOverlay.enableMiningStatsOverlay
val miningStatsOverlayInMiningIslandsOnly: Boolean get() = miningStatsOverlay.miningStatsOverlayInMiningIslandsOnly
val coleweightRankingInChat: Boolean get() = coleweightConfig.coleweightRankingInChat
val onlyOnMiningIslands: Boolean get() = coleweightConfig.onlyOnMiningIslands
val showDetailedMiningFortune : Boolean get() = miningStatsOverlay.showDetailedFortune
val professionalMS: Property<Int> get() = hotmConfig.professionalMS
val strongArmMS: Property<Int> get() = hotmConfig.strongArmMS

// Foraging Config Accessors
val foragingConfig: Foraging get() = modConfig.foraging
val foragingOverlay: ForagingStatsOverlay get() = foragingConfig.foragingStatsOverlay
val enableForagingStatsOverlay: Boolean get() = foragingOverlay.enableForagingStatsOverlay
val showDetailedForagingFortune: Boolean get() = foragingOverlay.showDetailedFortune

// Tracking Config Accessors
val trackingConfig: Tracking get() = modConfig.trackingOverlay
val collectionOverlay: CollectionOverlay get() = trackingConfig.collectionOverlay
val enableSacksTracking: Boolean get() = collectionOverlay.enableSacksTracking
val statsText: List<CollectionOverlay.OverlayText> get() = collectionOverlay.statsText
val extraStatsText: List<CollectionOverlay.OverlayExtraText> get() = collectionOverlay.extraStatsText
val showExtraStats: Boolean get() = collectionOverlay.showExtraStats
val explicitValues: Boolean get() = trackingConfig.explicitValues

// Skills Tracking Config Accessors
val skillOverlay: SkillOverlay get() = trackingConfig.skillOverlay
val enableTamingTracking: Boolean get() = skillOverlay.enableTamingTracking

/**
 * Accessors for configuration sections.
 */
object ConfigAccess {

    @JvmStatic
    fun getTrackingPosition(): Position = trackingPosition

    @JvmStatic
    fun getMiningStatsPosition(): Position = miningStatsPosition

    @JvmStatic
    fun getForagingStatsPosition(): Position = foragingStatsPosition

    @JvmStatic
    fun getCommissionsPosition(): Position = commissionsPosition

    @JvmStatic
    fun getSkillPosition(): Position = skillPosition

    @JvmStatic
    fun getUpdateType(): About.UpdateType = updateType

    @JvmStatic
    fun getBazaarType(): Bazaar.BazaarType = bazaarType

    @JvmStatic
    fun getGemstoneVariant(): Bazaar.GemstoneVariant = gemstoneVariant

    @JvmStatic
    fun isUsingBazaar(): Boolean = useBazaar

    @JvmStatic
    fun getKeybindConfig(): KeybindConfig = keybindConfig

    @JvmStatic
    fun hasCheckedUpdate(): Boolean = hasCheckedUpdate

    @JvmStatic
    fun isOverlayTextColorEnabled(): Boolean = collectionOverlay.overlayTextColor

    @JvmStatic
    fun isShowTrackingRatesAtEndOfSession(): Boolean = collectionOverlay.showTrackingRatesAtEndOfSession

    @JvmStatic
    fun isCommissionsEnabled(): Boolean = commissionsOverlay.enableCommissionsOverlay

    @JvmStatic
    fun isCommissionsKeybindsEnabled(): Boolean = keybindConfig.enableCommissionsKeybinds

    @JvmStatic
    fun isMiningStatsEnabled(): Boolean = enableMiningStatsOverlay

    @JvmStatic
    fun isMiningStatsOverlayInMiningIslandsOnly(): Boolean = miningStatsOverlayInMiningIslandsOnly

    @JvmStatic
    fun getStatsText(): List<CollectionOverlay.OverlayText> = statsText

    @JvmStatic
    fun isShowExtraStats(): Boolean = showExtraStats

    @JvmStatic
    fun getExtraStatsText(): List<CollectionOverlay.OverlayExtraText> = extraStatsText

    @JvmStatic
    fun isExplicitValues(): Boolean = explicitValues

    @JvmStatic
    fun isSacksTrackingEnabled(): Boolean = enableSacksTracking

    @JvmStatic
    fun isTamingTrackingEnabled(): Boolean = enableTamingTracking

    @JvmStatic
    fun getBazaarPriceType(): Bazaar.BazaarPriceType = bazaarPriceType

    @JvmStatic
    fun isColeweightRankingInChat(): Boolean = coleweightRankingInChat

    @JvmStatic
    fun isOnlyOnMiningIslands(): Boolean = onlyOnMiningIslands

    @JvmStatic
    fun isShowDetailedMiningFortune(): Boolean = showDetailedMiningFortune

    @JvmStatic
    fun getProfessionalMS(): Int = professionalMS.get()

    @JvmStatic
    fun getStrongArmMS(): Int = strongArmMS.get()

    @JvmStatic
    fun isForagingStatsOverlayEnabled(): Boolean = enableForagingStatsOverlay

    @JvmStatic
    fun isShowDetailedForagingFortune(): Boolean = showDetailedForagingFortune
}

/**
 * Utility helper for common configuration operations.
 */
object ConfigHelper {

    @JvmStatic
    fun setBazaarType(type: Bazaar.BazaarType) {
        bazaarConfig.bazaarType = type
    }

    @JvmStatic
    fun enableUpdateChecks() {
        aboutConfig.hasCheckedUpdate = true
    }

    @JvmStatic
    fun disableUpdateChecks() {
        aboutConfig.hasCheckedUpdate = false
    }

    @JvmStatic
    fun disableBazaar() {
        bazaarConfig.useBazaar = false
    }

    @JvmStatic
    fun disableExtraStats() {
        collectionOverlay.showExtraStats = false
    }

    @JvmStatic
    fun disableCommissions() {
        commissionsOverlay.enableCommissionsOverlay = false
    }

    @JvmStatic
    fun disableMiningStats() {
        miningConfig.miningStatsOverlay.enableMiningStatsOverlay = false
    }

    @JvmStatic
    fun disableTamingTracking() {
        skillOverlay.enableTamingTracking = false
    }

    @JvmStatic
    fun disableForagingStats() {
        foragingOverlay.enableForagingStatsOverlay = false
    }
}