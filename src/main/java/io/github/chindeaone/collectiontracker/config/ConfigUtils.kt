package io.github.chindeaone.collectiontracker.config

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.categories.About
import io.github.chindeaone.collectiontracker.config.categories.Bazaar
import io.github.chindeaone.collectiontracker.config.categories.Farming
import io.github.chindeaone.collectiontracker.config.categories.Foraging
import io.github.chindeaone.collectiontracker.config.categories.farmingweight.Farmingweight
import io.github.chindeaone.collectiontracker.config.categories.Mining
import io.github.chindeaone.collectiontracker.config.categories.Misc
import io.github.chindeaone.collectiontracker.config.categories.Tracking
import io.github.chindeaone.collectiontracker.config.categories.coleweight.Coleweight
import io.github.chindeaone.collectiontracker.config.categories.coleweight.ColeweightColor
import io.github.chindeaone.collectiontracker.config.categories.coleweight.HeatmapConfig
import io.github.chindeaone.collectiontracker.config.categories.coleweight.PrecisionMiningConfig
import io.github.chindeaone.collectiontracker.config.categories.farmingweight.FarmingweightColor
import io.github.chindeaone.collectiontracker.config.categories.foraging.AxeAbilityConfig
import io.github.chindeaone.collectiontracker.config.categories.foraging.HotfConfig
import io.github.chindeaone.collectiontracker.config.categories.foraging.LotteryConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.HotmConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.KeybindConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.LanternDeployable
import io.github.chindeaone.collectiontracker.config.categories.mining.routes.MineshaftRoutes
import io.github.chindeaone.collectiontracker.config.categories.mining.routes.MiningRoutesConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.PickaxeAbilityConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.SkyMallConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.TemporaryBuffsConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.routes.DwarvenMetalsRoutes
import io.github.chindeaone.collectiontracker.config.categories.mining.routes.PureOresRoutes
import io.github.chindeaone.collectiontracker.config.categories.overlay.CollectionOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.CommissionsOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.ForagingStatsOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.MiningStatsOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.MultiCollectionOverlay
import io.github.chindeaone.collectiontracker.config.categories.overlay.SkillOverlay
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.ChromaColour
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
val deployablePosition: Position get() = modConfig.mining.lanternDeployable.deployablePosition
val tempBuffPosition: Position get() = modConfig.mining.temporaryBuffsConfig.tempBuffPosition
val titlePosition: Position get() = modConfig.misc.titlePosition
val multiOverlayPosition: Position get() = modConfig.trackingOverlay.multiCollectionOverlay.multiOverlayPosition
val coleweightTimerPosition: Position get() = modConfig.mining.coleweight.coleweightTimerPosition
val coleweightTrackerPosition: Position get() = modConfig.mining.coleweight.coleweightTrackerPosition

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
val miningRoutesConfig: MiningRoutesConfig get() = miningConfig.miningRoutesConfig
val commissionsOverlay: CommissionsOverlay get() = miningConfig.commissionsOverlay
val keybindConfig: KeybindConfig get() = commissionsOverlay.commissions
val hotmConfig: HotmConfig get() = miningConfig.hotmConfig
val skyMallConfig: SkyMallConfig get() = hotmConfig.skyMallConfig
val pickaxeAbilityConfig: PickaxeAbilityConfig get() = hotmConfig.pickaxeAbilityConfig
val lanternDeployable: LanternDeployable get() = miningConfig.lanternDeployable
val temporaryBuffsConfig: TemporaryBuffsConfig get() = miningConfig.temporaryBuffsConfig
val cotmLevel: Property<Int> get() = hotmConfig.cotmLevel
val displayPickaxeAbility: Boolean get() = hotmConfig.pickaxeAbilityConfig.displayPickaxeAbility
val abilityName: String get() = pickaxeAbilityConfig.abilityName
val lastPet: String get() = pickaxeAbilityConfig.lastPet
val enableSkyMall: Boolean get() = skyMallConfig.enableSkyMall
val lastSkyMallPerk: String get() = skyMallConfig.lastSkyMallPerk
val skyMallInMiningIslandsOnly: Boolean get() = skyMallConfig.skyMallInMiningIslandsOnly
val disableSkyMallChatMessages: Boolean get() = skyMallConfig.disableSkyMallChatMessages
val showPickaxeReadyAbilityTitle: Boolean get() = pickaxeAbilityConfig.showPickaxeReadyAbilityTitle
val showPickaxeExpiredAbilityTitle: Boolean get() = pickaxeAbilityConfig.showPickaxeExpiredAbilityTitle
val pickaxeAbilityInMiningIslandsOnly: Boolean get() = pickaxeAbilityConfig.pickaxeAbilityInMiningIslandsOnly
val mineshaftRoutesConfig: MineshaftRoutes get() = miningRoutesConfig.mineshaftRoutes
val enableMineshaftRoutes: Boolean get() = mineshaftRoutesConfig.enableMineshaftRoutes
val enableMineshaftSpawnRoutes: Boolean get() = mineshaftRoutesConfig.enableMineshaftSpawnRoutes
val mineshaftSpawnRoutes: MineshaftRoutes.MineshaftSpawnRoutes get() = mineshaftRoutesConfig.selectedMineshaftSpawnRoute
val dwarvenMetalsRoutesConfig: DwarvenMetalsRoutes get() = miningRoutesConfig.dwarvenMetalsRoutes
val enableDwarvenMetalRoutes: Boolean get() = dwarvenMetalsRoutesConfig.enableDwarvenMetalRoutes
val dwarvenMetalRoutes: DwarvenMetalsRoutes.DwarvenMetalRoutes get() = dwarvenMetalsRoutesConfig.selectedDwarvenMetalRoute
val pureOresRoutesConfig: PureOresRoutes get() = miningRoutesConfig.pureOresRoutes
val enablePureOresRoutes: Boolean get() = pureOresRoutesConfig.enablePureOresRoutes
val pureOresRoutes: PureOresRoutes.PureOreRoutes get() = pureOresRoutesConfig.selectedPureOresRoute
val enableTempBuffTracker: Boolean get() = temporaryBuffsConfig.enableTempBuffTracker
val refinedCacaoTime: Long get() = temporaryBuffsConfig.refinedCacaoTime
val filetTime: Long get() = temporaryBuffsConfig.filetTime
val pristinePotatoTime: Long get() = temporaryBuffsConfig.pristinePotatoTime
val powderPumpkinTime: Long get() = temporaryBuffsConfig.powderPumpkinTime

// Coleweight Config Accessors
val coleweightConfig: Coleweight get() = miningConfig.coleweight
val coleweightAbilityFormat: Boolean get() = coleweightConfig.coleweightAbilityFormat
val coleweightColor: ColeweightColor get() = coleweightConfig.coleweightColor
val enableCustomCWColor: Boolean get() = coleweightColor.enableCustomColor
val customCWColor: ChromaColour get() = coleweightColor.customColor
val heatmapConfig: HeatmapConfig get() = coleweightConfig.heatmapConfig
val enableHeatmap: Boolean get() = heatmapConfig.enableHeatmap
val heatmapOpacity: Property<Float> get() = heatmapConfig.heatmapOpacity
val precisionMiningConfig: PrecisionMiningConfig get() = coleweightConfig.precisionMiningConfig
val enablePrecisionMiningHighlight: Boolean get() = precisionMiningConfig.enablePrecisionMiningHighlight
val drawLineToPrecisionMining: Boolean get() = precisionMiningConfig.drawLineToPrecisionMining

// Overlays
val miningStatsOverlay: MiningStatsOverlay get() = miningConfig.miningStatsOverlay
val enableMiningStatsOverlay: Boolean get() = miningStatsOverlay.enableMiningStatsOverlay
val miningStatsOverlayInMiningIslandsOnly: Boolean get() = miningStatsOverlay.miningStatsOverlayInMiningIslandsOnly
val coleweightRankingInChat: Boolean get() = coleweightConfig.coleweightRankingInChat
val onlyOnMiningIslands: Boolean get() = coleweightConfig.onlyOnMiningIslands
val showDetailedMiningFortune : Boolean get() = miningStatsOverlay.showDetailedFortune
val professionalMS: Property<Int> get() = hotmConfig.professionalMS
val strongArmMS: Property<Int> get() = hotmConfig.strongArmMS
val enableDeployable: Boolean get() = lanternDeployable.enableDeployable
val showDeployableTitle: Boolean get() = lanternDeployable.showDeployableTitle

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
val showAxeReadyAbilityTitle: Boolean get() = axeAbilityConfig.showAxeReadyAbilityTitle
val showAxeExpiredAbilityTitle: Boolean get() = axeAbilityConfig.showAxeExpiredAbilityTitle
val axeAbilityInForagingIslandsOnly: Boolean get() = axeAbilityConfig.axeAbilityInForagingIslandsOnly

// Farming Config Accessors
val farmingConfig: Farming get() = modConfig.farming
val farmingweightConfig: Farmingweight get() = farmingConfig.farmingweight
val farmingweightRankingInChat: Boolean get() = farmingweightConfig.farmingweightRankingInChat
val onlyOnFarmingIslands: Boolean get() = farmingweightConfig.onlyOnFarmingIslands
val farmingweightColor: FarmingweightColor get() = farmingweightConfig.farmingweightColor
val customFWColor: ChromaColour get() = farmingweightColor.customColor
val enableCustomFWColor: Boolean get() = farmingweightColor.enableCustomColor

// Collection Tracking Config Accessors
val trackingConfig: Tracking get() = modConfig.trackingOverlay
val collectionOverlay: CollectionOverlay get() = trackingConfig.collectionOverlay
val statsText: List<CollectionOverlay.OverlayText> get() = collectionOverlay.statsText
val extraStatsText: List<CollectionOverlay.OverlayExtraText> get() = collectionOverlay.extraStatsText
val showExtraStats: Boolean get() = collectionOverlay.showExtraStats
val explicitValues: Boolean get() = trackingConfig.explicitValues
val leaderboardTracking: Boolean get() = collectionOverlay.leaderboardTracking

// Multi Collection Tracking Config Accessors
val multiCollectionOverlay: MultiCollectionOverlay get() = trackingConfig.multiCollectionOverlay
val trackingOptions: MultiCollectionOverlay.TrackingOptions get() = multiCollectionOverlay.trackingOptions
val multiTrackingSummary: Boolean get() = multiCollectionOverlay.multiTrackingSummary
val multiDetailedSummary: Boolean get() = multiCollectionOverlay.multiDetailedSummary
val summaryStats: MultiCollectionOverlay.SummaryStats get() = multiCollectionOverlay.summaryStats

// Skills Tracking Config Accessors
val skillOverlay: SkillOverlay get() = trackingConfig.skillOverlay
val enableTamingTracking: Boolean get() = skillOverlay.enableTamingTracking

// Misc Config Accessors
val miscConfig: Misc get() = modConfig.misc
val precision: Property<Int> get() = miscConfig.abilityPrecision
val titleDisplayTimer: Property<Int> get() = miscConfig.titleDisplayTimer
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
    fun getDeployablePosition(): Position = deployablePosition

    @JvmStatic
    fun getTempBuffPosition(): Position = tempBuffPosition

    @JvmStatic
    fun getTitlePosition(): Position = titlePosition

    @JvmStatic
    fun getMultiOverlayPosition(): Position = multiOverlayPosition

    @JvmStatic
    fun getColeweightTimerPosition(): Position = coleweightTimerPosition

    @JvmStatic
    fun getColeweightTrackerPosition(): Position = coleweightTrackerPosition

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
    fun isTamingTrackingEnabled(): Boolean = enableTamingTracking

    @JvmStatic
    fun getBazaarPriceType(): Bazaar.BazaarPriceType = bazaarPriceType

    @JvmStatic
    fun isColeweightRankingInChat(): Boolean = coleweightRankingInChat

    @JvmStatic
    fun isOnlyOnMiningIslands(): Boolean = onlyOnMiningIslands

    @JvmStatic
    fun isCustomCwColorEnabled(): Boolean = enableCustomCWColor

    @JvmStatic
    fun getCustomCWColor(): ChromaColour = customCWColor

    @JvmStatic
    fun isFarmingweightRankingInChat(): Boolean = farmingweightRankingInChat

    @JvmStatic
    fun isOnlyOnFarmingIslands(): Boolean = onlyOnFarmingIslands

    @JvmStatic
    fun isCustomFWColorEnabled(): Boolean = enableCustomFWColor

    @JvmStatic
    fun getCustomFWColor(): ChromaColour = customFWColor

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
    fun getTitleDisplayTimer(): Int = titleDisplayTimer.get()

    @JvmStatic
    fun isShowPickaxeReadyAbilityTitle(): Boolean = showPickaxeReadyAbilityTitle

    @JvmStatic
    fun isShowPickaxeExpiredAbilityTitle(): Boolean = showPickaxeExpiredAbilityTitle

    @JvmStatic
    fun isShowAxeReadyAbilityTitle(): Boolean = showAxeReadyAbilityTitle

    @JvmStatic
    fun isShowAxeExpiredAbilityTitle(): Boolean = showAxeExpiredAbilityTitle

    @JvmStatic
    fun getTitleScale(): Misc.TitleScale = titleScale

    @JvmStatic
    fun isPickaxeAbilityInMiningIslandsOnly(): Boolean = pickaxeAbilityInMiningIslandsOnly

    @JvmStatic
    fun isAxeAbilityInForagingIslandsOnly(): Boolean = axeAbilityInForagingIslandsOnly

    @JvmStatic
    fun isAbilityCooldownOnly(): Boolean = abilityCooldownOnly

    @JvmStatic
    fun isServerLagProtectionEnabled(): Boolean = miscConfig.serverLagProtection

    @JvmStatic
    fun isDeployableEnabled(): Boolean = enableDeployable

    @JvmStatic
    fun isShowDeployableTitle(): Boolean = showDeployableTitle

    @JvmStatic
    fun isMineshaftRoutesEnabled(): Boolean = enableMineshaftRoutes

    @JvmStatic
    fun isMineshaftSpawnRoutesEnabled(): Boolean = enableMineshaftSpawnRoutes

    @JvmStatic
    fun getMineshaftSpawnRoutes(): MineshaftRoutes.MineshaftSpawnRoutes = mineshaftSpawnRoutes

    @JvmStatic
    fun isDwarvenMetalRoutesEnabled(): Boolean = enableDwarvenMetalRoutes

    @JvmStatic
    fun getDwarvenMetalRoutes(): DwarvenMetalsRoutes.DwarvenMetalRoutes = dwarvenMetalRoutes

    @JvmStatic
    fun isPureOresRoutesEnabled(): Boolean = enablePureOresRoutes

    @JvmStatic
    fun getPureOresRoutes(): PureOresRoutes.PureOreRoutes = pureOresRoutes

    @JvmStatic
    fun isTempBuffTrackerEnabled(): Boolean = enableTempBuffTracker

    @JvmStatic
    fun getRefinedCacaoTime(): Long = refinedCacaoTime

    @JvmStatic
    fun getFiletTime(): Long = filetTime

    @JvmStatic
    fun getPristinePotatoTime(): Long = pristinePotatoTime

    @JvmStatic
    fun getPowderPumpkinTime(): Long = powderPumpkinTime

    @JvmStatic
    fun isHeatmapEnabled(): Boolean = enableHeatmap

    @JvmStatic
    fun getHeatmapOpacity(): Float = heatmapOpacity.get()

    @JvmStatic
    fun isPrecisionMiningHighlightEnabled(): Boolean = enablePrecisionMiningHighlight

    @JvmStatic
    fun isDrawLineToPrecisionMiningEnabled(): Boolean = drawLineToPrecisionMining

    @JvmStatic
    fun getTrackingOptions(): MultiCollectionOverlay.TrackingOptions = trackingOptions

    @JvmStatic
    fun isMultiTrackingSummaryEnabled(): Boolean = multiTrackingSummary

    @JvmStatic
    fun isMultiDetailedSummaryEnabled(): Boolean = multiDetailedSummary

    @JvmStatic
    fun getSummaryStats(): MultiCollectionOverlay.SummaryStats = summaryStats

    @JvmStatic
    fun isLeaderboardTrackingEnabled(): Boolean = leaderboardTracking

    @JvmStatic
    fun setMineshaftSpawnRoutesEnabled(enabled: Boolean) {
        mineshaftRoutesConfig.enableMineshaftSpawnRoutes = enabled
    }

    @JvmStatic
    fun setDwarvenMetalRoutesEnabled(enabled: Boolean) {
        dwarvenMetalsRoutesConfig.enableDwarvenMetalRoutes = enabled
    }

    @JvmStatic
    fun setPureOresRoutesEnabled(enabled: Boolean) {
        pureOresRoutesConfig.enablePureOresRoutes = enabled
    }
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
    fun disableLeaderboardTracking() {
        collectionOverlay.leaderboardTracking = false
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

    @JvmStatic
    fun setDuration(refined: Long = -1, filet: Long = -1, potato: Long = -1, pumpkin: Long = -1) {
        if (refined != -1L) temporaryBuffsConfig.refinedCacaoTime = refined
        if (filet != -1L) temporaryBuffsConfig.filetTime = filet
        if (potato != -1L) temporaryBuffsConfig.pristinePotatoTime = potato
        if (pumpkin != -1L) temporaryBuffsConfig.powderPumpkinTime = pumpkin
    }

    @JvmStatic
    fun setProfessionalMS(ms: Int) {
        hotmConfig.professionalMS.set(ms)
    }

    @JvmStatic
    fun setStrongArmMS(ms: Int) {
        hotmConfig.strongArmMS.set(ms)
    }

    @JvmStatic
    fun setCotmLevel(level: Int) {
        hotmConfig.cotmLevel.set(level)
    }

    @JvmStatic
    fun setCotfLevel(level: Int) {
        hotfConfig.cotfLevel.set(level)
    }

    @JvmStatic
    fun setTitlePosition(x: Int, y: Int) {
        modConfig.misc.titlePosition = Position(x, y)
    }

    @JvmStatic
    fun setBazaar(enabled: Boolean) {
        bazaarConfig.useBazaar = enabled
    }

    @JvmStatic
    fun setGemstoneVariant(variant: Bazaar.GemstoneVariant) {
        bazaarConfig.gemstoneVariant = variant
    }

    @JvmStatic
    fun setShowExtraStats(show: Boolean) {
        collectionOverlay.showExtraStats = show
    }

    @JvmStatic
    fun changeBazaarPrice(type: Bazaar.BazaarPriceType) {
        bazaarConfig.bazaarPriceType = type
    }

    @JvmStatic
    fun setColeweightCustomColor(player: String, color: String) {
        coleweightColor.customColors[player] = color
    }

    @JvmStatic
    fun getColeweightColor(player: String): String? {
        return coleweightColor.customColors[player]
    }

    @JvmStatic
    fun removeColeweightCustomColor(player: String) {
        coleweightColor.customColors.remove(player)
    }

    @JvmStatic
    fun setFarmingweightCustomColor(player: String, color: String) {
        farmingweightColor.customColors[player] = color
    }

    @JvmStatic
    fun getFarmingweightColor(player: String): String? {
        return farmingweightColor.customColors[player]
    }

    @JvmStatic
    fun removeFarmingweightColor(player: String) {
        farmingweightColor.customColors.remove(player)
    }
}