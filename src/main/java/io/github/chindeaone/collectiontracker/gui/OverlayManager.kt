package io.github.chindeaone.collectiontracker.gui

import io.github.chindeaone.collectiontracker.gui.overlays.*

object OverlayManager {

    private val overlays: MutableMap<String, AbstractOverlay> = linkedMapOf()
    private var globalRenderingAllowed: Boolean = true

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
    fun setTrackingOverlayRendering(allowed: Boolean) = setOverlayRendering(CollectionOverlay().overlayLabel(), allowed)

    fun setMultiTrackingOverlayRendering(allowed: Boolean) = setOverlayRendering(MultiCollectionOverlay().overlayLabel(), allowed)

    @JvmStatic
    fun setSkillOverlayRendering(allowed: Boolean) = setOverlayRendering(SkillOverlay().overlayLabel(), allowed)

    fun setColeweightOverlayRendering(allowed: Boolean) = setOverlayRendering(ColeweightOverlay().overlayLabel(), allowed)

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
                StopwatchOverlay(),
                ColeweightOverlay()
        ).forEach { add(it) }
    }

    @JvmStatic
    fun isCollectionOverlay(overlay: AbstractOverlay): Boolean {
        val label = overlay.overlayLabel()
        return label == CollectionOverlay().overlayLabel() || label == MultiCollectionOverlay().overlayLabel()
    }

    @JvmStatic
    fun getTimerOverlay(): TimerOverlay? = overlays.values.filterIsInstance<TimerOverlay>().firstOrNull()

    @JvmStatic
    fun getStopwatchOverlay(): StopwatchOverlay? = overlays.values.filterIsInstance<StopwatchOverlay>().firstOrNull()
}

