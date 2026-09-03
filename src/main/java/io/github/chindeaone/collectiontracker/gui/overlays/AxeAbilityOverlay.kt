package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getAxeAbilityDisplayIndicator
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getAxeAbilityName
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getAxeAbilityPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getTitleDisplayTimer
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isAbilityCooldownOnly
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isAxeAbilityDisplayed
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isAxeAbilityInForagingIslandsOnly
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isShowAxeExpiredAbilityTitle
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isShowAxeReadyAbilityTitle
import io.github.chindeaone.collectiontracker.config.categories.Misc
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.finalCooldown
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.finalDuration
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.renderCooldownBar
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.renderCooldownCircle
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.showTitle
import io.github.chindeaone.collectiontracker.utils.world.ForagingMapping.foragingIslands
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker.currentForagingIsland
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

class AxeAbilityOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private var lastCooldown: Double = -1.0
    private var lastDuration: Double = -1.0
    private var lastAbilityName: String = ""

    private var expiredTitleShown = true
    private var readyTitleShown = true

    override val overlayLabel: String = "Axe Ability"

    override val position: Position get() = getAxeAbilityPosition()

    override val isEnabled: Boolean get() = true

    override fun render(context: GuiGraphicsExtractor) {
        super.render(context)

        when (getAxeAbilityDisplayIndicator()) {
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

    private fun updateLinesIfNeeded() {
        if (!isAxeAbilityDisplayed() || (isAxeAbilityInForagingIslandsOnly() && !foragingIslands.contains(currentForagingIsland))) {
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
            }
            return
        }

        val abilityName = getAxeAbilityName()
        val cooldown = finalCooldown
        val active = finalDuration

        if (cachedLines.isNotEmpty() && cooldown == lastCooldown && active == lastDuration && abilityName == lastAbilityName) {
            return
        }

        lastCooldown = cooldown
        lastDuration = active
        lastAbilityName = abilityName

        val displayName = abilityName.ifEmpty { "Unknown Ability" }

        if (active > 0) {
            expiredTitleShown = false
            readyTitleShown = false
        }

        val newLines = mutableListOf<String>()

        if (active == 0.0) {
            if (isShowAxeExpiredAbilityTitle() && !expiredTitleShown && cooldown > 0) {
                val titleExpired = "§6[§3§kd§6] §b§l$displayName §cExpired! §6[§3§kd§6]" // Credit to Ninjune for Coleweight's formatting
                showTitle(Component.literal(titleExpired), getTitleDisplayTimer())
                expiredTitleShown = true
            }

            if (cooldown <= 0) {
                if (isShowAxeReadyAbilityTitle() && !readyTitleShown) {
                    val titleReady = "§6[§3§kd§6] §b§l$displayName §6[§3§kd§6]" // Credit to Ninjune for Coleweight's formatting
                    showTitle(Component.literal(titleReady), getTitleDisplayTimer())
                    readyTitleShown = true
                }
            } else {
                readyTitleShown = false
            }
        }

        val status = if (!isAbilityCooldownOnly() && active > 0) {
            "§a" + StringUtils.formatTimeInSeconds(active)
        } else if (cooldown > 0) {
            "§c" + StringUtils.formatTimeInSeconds(cooldown)
        } else {
            "§aReady!"
        }
        newLines.add("§e$displayName CD: $status") // // Credit to Ninjune for Coleweight's formatting

        cachedLines = newLines
    }
}