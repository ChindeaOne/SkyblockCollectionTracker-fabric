package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.config.ConfigAccess.getPickaxeAbilityDisplayIndicator
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getPickaxeAbilityName
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getPickaxeAbilityPosition
import io.github.chindeaone.collectiontracker.config.ConfigAccess.getTitleDisplayTimer
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isAbilityCooldownOnly
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isPickaxeAbilityDisplayed
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isPickaxeAbilityInMiningIslandsOnly
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isShowPickaxeExpiredAbilityTitle
import io.github.chindeaone.collectiontracker.config.ConfigAccess.isShowPickaxeReadyAbilityTitle
import io.github.chindeaone.collectiontracker.config.categories.Misc
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.finalCooldown
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.finalDuration
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.renderCooldownBar
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.renderCooldownCircle
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils.showTitle
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

class PickaxeAbilityOverlay : AbstractOverlay() {
    private var cachedLines: List<String> = emptyList()
    private var lastCooldown: Double = -1.0
    private var lastDuration: Double = -1.0
    private var lastAbilityName: String = ""

    private var expiredTitleShown = true
    private var readyTitleShown = true

    override val overlayLabel: String = "Pickaxe Ability"

    override val position: Position get() = getPickaxeAbilityPosition()

    override val isEnabled: Boolean get() = isPickaxeAbilityDisplayed() && (!isPickaxeAbilityInMiningIslandsOnly() || IslandTracker.isMiningIsland())

    override fun render(context: GuiGraphicsExtractor) {
        super.render(context)

        when (getPickaxeAbilityDisplayIndicator()) {
            Misc.AbilityDisplayIndicator.CROSSHAIR_CIRCLE -> renderCooldownCircle(context, "pickaxe")
            Misc.AbilityDisplayIndicator.CROSSHAIR_BAR -> renderCooldownBar(context, "pickaxe")
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
        if (!isPickaxeAbilityDisplayed() || (isPickaxeAbilityInMiningIslandsOnly() && !IslandTracker.isMiningIsland())) {
            if (cachedLines.isNotEmpty()) {
                cachedLines = emptyList()
            }
            return
        }

        val abilityName = getPickaxeAbilityName()
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
            if (isShowPickaxeExpiredAbilityTitle() && !expiredTitleShown && cooldown > 0 && (displayName != "Pickobulus")) {
                val titleExpired = "§6[§3§kd§6] §b§l$displayName §cExpired! §6[§3§kd§6]" // Credit to Ninjune for Coleweight's formatting
                showTitle(Component.literal(titleExpired), getTitleDisplayTimer())
                expiredTitleShown = true
            }
            if (cooldown <= 0) {
                if (isShowPickaxeReadyAbilityTitle() && !readyTitleShown) {
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
        newLines.add("§e$displayName CD: $status") // Credit to Ninjune for Coleweight's formatting

        cachedLines = newLines
    }
}
