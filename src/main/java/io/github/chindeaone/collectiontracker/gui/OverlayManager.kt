package io.github.chindeaone.collectiontracker.gui

import io.github.chindeaone.collectiontracker.gui.overlays.*

object OverlayManager {

    private val overlays: MutableMap<String, AbstractOverlay> = linkedMapOf()
    private var globalRenderingAllowed: Boolean = true

    private const val TRACKING_LABEL = "Collection Tracker"
    private const val MULTI_LABEL = "Multi-Collection Tracker"
    private const val SKILL_LABEL = "Skill Tracker"
    private const val COLEWEIGHT_LABEL = "Coleweight Tracker"

    fun add(overlay: AbstractOverlay) {
        overlays[overlay.overlayLabel()] = overlay
    }

    @JvmStatic
    fun all(): Collection<AbstractOverlay> = overlays.values

    fun setGlobalRendering(allowed: Boolean) {
        globalRenderingAllowed = allowed
        overlays.values.forEach { it.isRenderingAllowed = allowed }
    }

    fun isInEditorMode(): Boolean = !globalRenderingAllowed

    private fun setOverlayRendering(label: String, allowed: Boolean) {
        overlays[label]?.isRenderingAllowed = allowed
    }

    @JvmStatic
    fun setTrackingOverlayRendering(allowed: Boolean) = setOverlayRendering(TRACKING_LABEL, allowed)

    fun setMultiTrackingOverlayRendering(allowed: Boolean) = setOverlayRendering(MULTI_LABEL, allowed)

    @JvmStatic
    fun setSkillOverlayRendering(allowed: Boolean) = setOverlayRendering(SKILL_LABEL, allowed)

    fun setColeweightOverlayRendering(allowed: Boolean) = setOverlayRendering(COLEWEIGHT_LABEL, allowed)

    fun overlayRegistration() {
        listOf(
                CollectionOverlay(),
                MultiCollectionOverlay(),
                MiningStatsOverlay(),
                CommissionsOverlay(),
                SkillOverlay(),
                ForagingStatsOverlay(),
                SkyMallOverlay(),
                LotteryOverlay(),
                PickaxeAbilityOverlay(),
                AxeAbilityOverlay(),
                DeployableOverlay(),
                TemporaryBuffsOverlay(),
                TitleOverlay(),
                TimerOverlay(),
                ColeweightOverlay()
        ).forEach { add(it) }
    }

    @JvmStatic
    fun isCollectionOverlay(overlay: AbstractOverlay): Boolean {
        val label = overlay.overlayLabel()
        return label == TRACKING_LABEL || label == MULTI_LABEL
    }

    @JvmStatic
    fun getTimerOverlay(): TimerOverlay? = overlays.values.filterIsInstance<TimerOverlay>().firstOrNull()
}

