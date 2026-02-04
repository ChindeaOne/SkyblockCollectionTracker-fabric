package io.github.chindeaone.collectiontracker.config

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.categories.About
import io.github.chindeaone.collectiontracker.config.categories.Mining
import io.github.chindeaone.collectiontracker.config.categories.Tracking
import io.github.chindeaone.collectiontracker.config.categories.bazaar.BazaarConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.KeybindConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.CollectionOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.CommissionsOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.MiningStatsOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.SkillOverlay
import io.github.chindeaone.collectiontracker.config.core.Position

/**
 * Global accessors for the configuration.
 */
val modConfig: ModConfig get() = SkyblockCollectionTracker.configManager.config!!

// Position Config Accessor
val trackingPosition: Position get() = modConfig.trackingOverlay.collectionOverlay.overlayPosition
val miningStatsPosition: Position get() = modConfig.mining.miningStatsOverlay.miningStatsOverlayPosition
val commissionsPosition: Position get() = modConfig.mining.commissionsOverlay.commissionsOverlayPosition
val skillPosition: Position get() = modConfig.trackingOverlay.skillOverlay.skillOverlayPosition

// About Config Accessor
val aboutConfig: About get() = modConfig.about
val updateType: About.UpdateType get() = aboutConfig.update
val hasCheckedUpdate: Boolean get() = aboutConfig.hasCheckedUpdate

// Bazaar Config Accessors
val bazaarConfig: BazaarConfig get() = modConfig.bazaar.bazaarConfig
val useBazaar: Boolean get() = bazaarConfig.useBazaar
val bazaarType: BazaarConfig.BazaarType get() = bazaarConfig.bazaarType
val gemstoneVariant: BazaarConfig.GemstoneVariant get() = bazaarConfig.gemstoneVariant

// Mining Config Accessors
val miningConfig: Mining get() = modConfig.mining
// Overlays
val keybindConfig: KeybindConfig get() = miningConfig.commissionsOverlay.commissions
val commissionsOverlay: CommissionsOverlay get() = miningConfig.commissionsOverlay
val miningStatsOverlay: MiningStatsOverlay get() = miningConfig.miningStatsOverlay
val enableMiningStatsOverlay: Boolean get() = miningStatsOverlay.enableMiningStatsOverlay
val miningStatsOverlayInMiningIslandsOnly: Boolean get() = miningStatsOverlay.miningStatsOverlayInMiningIslandsOnly

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
    fun getCommissionsPosition(): Position = commissionsPosition

    @JvmStatic
    fun getSkillPosition(): Position = skillPosition

    @JvmStatic
    fun getUpdateType(): About.UpdateType = updateType

    @JvmStatic
    fun getBazaarType(): BazaarConfig.BazaarType = bazaarType

    @JvmStatic
    fun getGemstoneVariant(): BazaarConfig.GemstoneVariant = gemstoneVariant

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
}

/**
 * Utility helper for common configuration operations.
 */
object ConfigHelper {

    @JvmStatic
    fun setBazaarType(type: BazaarConfig.BazaarType) {
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
}