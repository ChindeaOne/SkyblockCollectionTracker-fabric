package io.github.chindeaone.collectiontracker.gui.overlays

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.config.abilityCooldownOnly
import io.github.chindeaone.collectiontracker.config.abilityName
import io.github.chindeaone.collectiontracker.config.categories.Misc
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.config.displayPickaxeAbility
import io.github.chindeaone.collectiontracker.config.pickaxeAbilityDisplayIndicator
import io.github.chindeaone.collectiontracker.config.pickaxeAbilityInMiningIslandsOnly
import io.github.chindeaone.collectiontracker.config.pickaxeAbilityPosition
import io.github.chindeaone.collectiontracker.config.showPickaxeExpiredAbilityTitle
import io.github.chindeaone.collectiontracker.config.showPickaxeReadyAbilityTitle
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

    private var expiredTitleShown = true
    private var readyTitleShown = true

    override val overlayLabel: String = "Pickaxe Ability"

    override val position: Position get() = pickaxeAbilityPosition

    override val isEnabled: Boolean get() = displayPickaxeAbility && (!pickaxeAbilityInMiningIslandsOnly || IslandTracker.isMiningIsland())

    override fun render(context: GuiGraphicsExtractor) {
        super.render(context)

        if (!isEnabled) return
        when (pickaxeAbilityDisplayIndicator) {
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
        if (!isEnabled) {
            cachedLines = emptyList()
            return
        }

        if (ModLoader.clientTicks % 5L != 0L) return

        val abilityName = abilityName
        val cooldown = finalCooldown
        val active = finalDuration

        val displayName = abilityName.ifEmpty { "Unknown Ability" }

        if (active > 0) {
            expiredTitleShown = false
            readyTitleShown = false
        }

        if (active == 0.0) {
            if (showPickaxeExpiredAbilityTitle && !expiredTitleShown && cooldown > 0 && (displayName != "Pickobulus")) {
                val titleExpired = "§6[§3§kd§6] §b§l$displayName §cExpired! §6[§3§kd§6]" // Credit to Ninjune for Coleweight's formatting
                showTitle(Component.literal(titleExpired))
                expiredTitleShown = true
            }
            if (cooldown <= 0) {
                if (showPickaxeReadyAbilityTitle && !readyTitleShown) {
                    val titleReady = "§6[§3§kd§6] §b§l$displayName §6[§3§kd§6]" // Credit to Ninjune for Coleweight's formatting
                    showTitle(Component.literal(titleReady))
                    readyTitleShown = true
                }
            } else {
                readyTitleShown = false
            }
        }

        val status = if (!abilityCooldownOnly && active > 0) {
            "§a" + StringUtils.formatTimeInSeconds(active)
        } else if (cooldown > 0) {
            "§c" + StringUtils.formatTimeInSeconds(cooldown)
        } else {
            "§aReady!"
        }

        cachedLines = listOf("§e$displayName CD: $status") // Credit to Ninjune for Coleweight's formatting
    }
}
