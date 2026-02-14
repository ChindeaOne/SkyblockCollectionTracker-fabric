package io.github.chindeaone.collectiontracker.config

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.categories.About
import io.github.chindeaone.collectiontracker.config.categories.Bazaar
import io.github.chindeaone.collectiontracker.config.categories.Foraging
import io.github.chindeaone.collectiontracker.config.categories.Mining
import io.github.chindeaone.collectiontracker.config.categories.Misc
import io.github.chindeaone.collectiontracker.config.categories.Tracking
import io.github.chindeaone.collectiontracker.config.categories.coleweight.Coleweight
import io.github.chindeaone.collectiontracker.config.categories.foraging.AxeAbilityConfig
import io.github.chindeaone.collectiontracker.config.categories.foraging.HotfConfig
import io.github.chindeaone.collectiontracker.config.categories.foraging.LotteryConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.HotmConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.KeybindConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.PickaxeAbilityConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.SkyMallConfig
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
val skyMallPosition: Position get() = modConfig.mining.hotmConfig.skyMallConfig.skyMallPosition
val lotteryPosition: Position get() = modConfig.foraging.hotfConfig.lotteryConfig.lotteryPosition
val pickaxeAbilityPosition: Position get() = modConfig.mining.hotmConfig.pickaxeAbilityConfig.pickaxeAbilityPosition
val axeAbilityPosition: Position get() = modConfig.foraging.hotfConfig.axeAbilityConfig.axeAbilityPosition

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
val coleweightAbilityFormat: Boolean get() = coleweightConfig.coleweightAbilityFormat
val hotmConfig: HotmConfig get() = miningConfig.hotmConfig
val skyMallConfig: SkyMallConfig get() = hotmConfig.skyMallConfig
val pickaxeAbilityConfig: PickaxeAbilityConfig get() = hotmConfig.pickaxeAbilityConfig
val cotmLevel: Property<Int> get() = hotmConfig.cotmLevel
val displayPickaxeAbility: Boolean get() = hotmConfig.pickaxeAbilityConfig.displayPickaxeAbility
val abilityName: String get() = pickaxeAbilityConfig.abilityName
val lastPet: String get() = pickaxeAbilityConfig.lastPet
val enableSkyMall: Boolean get() = skyMallConfig.enableSkyMall
val lastSkyMallPerk: String get() = skyMallConfig.lastSkyMallPerk
val skyMallInMiningIslandsOnly: Boolean get() = skyMallConfig.skyMallInMiningIslandsOnly
val disableSkyMallChatMessages: Boolean get() = skyMallConfig.disableSkyMallChatMessages
val showPickaxeAbilityTitle: Boolean get() = pickaxeAbilityConfig.showPickaxeAbilityTitle
val pickaxeAbilityInMiningIslandsOnly: Boolean get() = pickaxeAbilityConfig.pickaxeAbilityInMiningIslandsOnly

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
val hotfConfig: HotfConfig get() = foragingConfig.hotfConfig
val axeAbilityConfig: AxeAbilityConfig get() = hotfConfig.axeAbilityConfig
val cotfLevel: Property<Int> get() = hotfConfig.cotfLevel
val abilityNameAxe: String get() = axeAbilityConfig.abilityNameAxe
val displayAxeAbility: Boolean get() = axeAbilityConfig.displayAxeAbility
val lotteryConfig: LotteryConfig get() = hotfConfig.lotteryConfig
val enableLottery: Boolean get() = lotteryConfig.enableLottery
val lotteryInForagingIslandsOnly: Boolean get() = lotteryConfig.lotteryInForagingIslandsOnly
val disableLotteryChatMessages: Boolean get() = lotteryConfig.disableLotteryChatMessages
val showAxeAbilityTitle: Boolean get() = axeAbilityConfig.showAxeAbilityTitle
val axeAbilityInForagingIslandsOnly: Boolean get() = axeAbilityConfig.axeAbilityInForagingIslandsOnly

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

// Misc Config Accessors
val miscConfig: Misc get() = modConfig.misc
val precision: Property<Int> get() = miscConfig.abilityPrecision
val abilityTitleDisplayTimer: Property<Int> get() = miscConfig.abilityTitleDisplayTimer
val titleScale: Misc.TitleScale get() = miscConfig.titleScale
val abilityCooldownOnly: Boolean get() = miscConfig.abilityCooldownOnly

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
    fun getSkyMallPosition(): Position = skyMallPosition

    @JvmStatic
    fun getLotteryPosition(): Position = lotteryPosition

    @JvmStatic
    fun getSkillPosition(): Position = skillPosition

    @JvmStatic
    fun getPickaxeAbilityPosition(): Position = pickaxeAbilityPosition

    @JvmStatic
    fun getAxeAbilityPosition(): Position = axeAbilityPosition

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

    @JvmStatic
    fun getCotmLevel(): Int = cotmLevel.get()

    @JvmStatic
    fun isSkyMallEnabled(): Boolean = enableSkyMall

    @JvmStatic
    fun isDisableSkyMallChatMessages(): Boolean = disableSkyMallChatMessages

    @JvmStatic
    fun isLotteryEnabled(): Boolean = enableLottery

    @JvmStatic
    fun isDisableLotteryChatMessages(): Boolean = disableLotteryChatMessages

    @JvmStatic
    fun isSkyMallInMiningIslandsOnly(): Boolean = skyMallInMiningIslandsOnly

    @JvmStatic
    fun isLotteryInForagingIslandsOnly(): Boolean = lotteryInForagingIslandsOnly

    @JvmStatic
    fun isPickaxeAbilityDisplayed(): Boolean = displayPickaxeAbility

    @JvmStatic
    fun getPickaxeAbilityName(): String = abilityName

    @JvmStatic
    fun getLastPet(): String = lastPet

    @JvmStatic
    fun getLastSkyMallPerk(): String = lastSkyMallPerk

    @JvmStatic
    fun getLastLotteryPerk(): String = lotteryConfig.lastLotteryPerk

    @JvmStatic
    fun isAxeAbilityDisplayed(): Boolean = displayAxeAbility

    @JvmStatic
    fun getAxeAbilityName(): String = abilityNameAxe

    @JvmStatic
    fun getCotfLevel(): Int = cotfLevel.get()

    @JvmStatic
    fun getAbilityPrecision(): Int = precision.get()

    @JvmStatic
    fun isColeweightAbilityFormat(): Boolean = coleweightAbilityFormat

    @JvmStatic
    fun getAbilityTitleDisplayTimer(): Int = abilityTitleDisplayTimer.get()

    @JvmStatic
    fun isShowPickaxeAbilityTitle(): Boolean = showPickaxeAbilityTitle

    @JvmStatic
    fun isShowAxeAbilityTitle(): Boolean = showAxeAbilityTitle

    @JvmStatic
    fun getTitleScale(): Misc.TitleScale = titleScale

    @JvmStatic
    fun isPickaxeAbilityInMiningIslandsOnly(): Boolean = pickaxeAbilityInMiningIslandsOnly

    @JvmStatic
    fun isAxeAbilityInForagingIslandsOnly(): Boolean = axeAbilityInForagingIslandsOnly

    @JvmStatic
    fun isAbilityCooldownOnly(): Boolean = abilityCooldownOnly
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

    @JvmStatic
    fun setAbilityName(name: String) {
        pickaxeAbilityConfig.abilityName = name
    }

    @JvmStatic
    fun setLastPet(pet: String) {
        pickaxeAbilityConfig.lastPet = pet
    }

    @JvmStatic
    fun setLastSkyMallPerk(perk: String) {
        skyMallConfig.lastSkyMallPerk = perk
    }

    @JvmStatic
    fun setLastLotteryPerk(perk: String) {
        lotteryConfig.lastLotteryPerk = perk
    }

    @JvmStatic
    fun setAxeAbilityName(name: String) {
        axeAbilityConfig.abilityNameAxe = name
    }
}