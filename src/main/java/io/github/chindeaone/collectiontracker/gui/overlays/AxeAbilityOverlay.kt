package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.abilityCooldownOnly
import io.github.chindeaone.collectiontracker.config.axeAbilityDisplayIndicator
import io.github.chindeaone.collectiontracker.config.axeAbilityInForagingIslandsOnly
import io.github.chindeaone.collectiontracker.config.axeAbilityPosition
import io.github.chindeaone.collectiontracker.config.categories.Misc
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.displayAxeAbility
import io.github.chindeaone.collectiontracker.config.showAxeExpiredAbilityTitle
import io.github.chindeaone.collectiontracker.config.showAxeReadyAbilityTitle
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.ability.AbilityUtils
import io.github.chindeaone.collectiontracker.utils.hotx.HotfData
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils.renderCooldownBar
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils.renderCooldownCircle
import io.github.chindeaone.collectiontracker.utils.render.RenderUtils.showTitle
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

class AxeAbilityOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()

    private var expiredTitleShown = true
    private var readyTitleShown = true

    override val overlayLabel: String = "Axe Ability"

    override val position: Position get() = axeAbilityPosition

    override val isEnabled: Boolean get() = displayAxeAbility && (!axeAbilityInForagingIslandsOnly || IslandTracker.isForagingIsland())

    override fun render(context: GuiGraphicsExtractor) {
        super.render(context)

        if (!isEnabled) return
        when (axeAbilityDisplayIndicator) {
            Misc.AbilityDisplayIndicator.CROSSHAIR_CIRCLE -> renderCooldownCircle(context, "axe")
            Misc.AbilityDisplayIndicator.CROSSHAIR_BAR -> renderCooldownBar(context, "axe")
            else -> {}
        }
    }

    override fun updateDimensions() {
        if (!isEnabled) return
        updateLinesIfNeeded()

        super.updateDimensions()
    }

    override val lines: List<String>
        get() {
            updateLinesIfNeeded()
            return cachedLines
        }

    /* Credit to Ninjune for Coleweight's formatting */
    private fun updateLinesIfNeeded() {
        if (!isEnabled) {
            cachedLines = emptyList()
            return
        }

        val ability = HotfData.currentAxeAbility
        val abilityName = ability.abilityName
        val cooldown = AbilityUtils.finalAxeCooldown
        val duration = AbilityUtils.finalAxeDuration

        if (duration > 0) {
            expiredTitleShown = false
            readyTitleShown = false
        }

        if (duration == 0.0) {
            if (showAxeExpiredAbilityTitle && !expiredTitleShown && cooldown > 0) {
                val titleExpired = "§6[§3§kd§6] §b§l$abilityName §cExpired! §6[§3§kd§6]"
                showTitle(Component.literal(titleExpired))
                expiredTitleShown = true
            }

            if (cooldown <= 0) {
                if (showAxeReadyAbilityTitle && !readyTitleShown) {
                    val titleReady = "§6[§3§kd§6] §b§l$abilityName §6[§3§kd§6]"
                    showTitle(Component.literal(titleReady))
                    readyTitleShown = true
                }
            } else {
                readyTitleShown = false
            }
        }

        val status = when {
            !abilityCooldownOnly && duration > 0 -> "§a" + StringUtils.formatTimeInSeconds(duration)
            cooldown > 0 -> "§c" + StringUtils.formatTimeInSeconds(cooldown)
            else -> "§aReady!"
        }

        cachedLines = listOf("§e$abilityName: $status")
    }
}