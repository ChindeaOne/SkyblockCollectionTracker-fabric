/*
 * Copyright (C) 2022-2023 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.tracker.commissions.CommissionsTracker
import io.github.chindeaone.collectiontracker.utils.parser.AbilityItemParser
import io.github.chindeaone.collectiontracker.utils.parser.CommissionFormat
import io.github.chindeaone.collectiontracker.utils.tab.CommissionWidget
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.ContainerListener
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.glfw.GLFW
import java.util.*

object CommissionKeybinds {

    private var lastClick = -1L
    private var openedAt = 0L

    private val keybinds: List<Int> get() = listOf(
        ConfigAccess.getKeybindConfig().commission1,
        ConfigAccess.getKeybindConfig().commission2,
        ConfigAccess.getKeybindConfig().commission3,
        ConfigAccess.getKeybindConfig().commission4
    )
    private val logger: Logger = LogManager.getLogger(CommissionKeybinds::class.java)

    private var attachedMenu: AbstractContainerMenu? = null
    private val wasDown = HashMap<Int, Boolean>()
    private const val CLICK_DEBOUNCE_MS = 300L

    private val COMMISSION_SLOTS = mapOf(11 to 0, 12 to 1, 14 to 2, 15 to 3)

    private var keyGuardActive = false
    private val guardedScreens: MutableSet<Screen> =
        Collections.newSetFromMap(WeakHashMap())

    private val menuListener = object : ContainerListener {
        override fun slotChanged(menu: AbstractContainerMenu, slotId: Int, stack: ItemStack) {
            val commissionIndex = COMMISSION_SLOTS[slotId] ?: return
            if (stack.isEmpty) return

            val client = Minecraft.getInstance()
            val player = client.player ?: return
            val level = client.level ?: return

            val tooltipContext = Item.TooltipContext.of(level.registryAccess())
            val tooltipLines = stack.getTooltipLines(
                tooltipContext,
                player,
                AbilityItemParser.tooltipFlag()
            ).map { it.string }

            val name = findCommissionName(tooltipLines) ?: return
            val progress = findProgress(tooltipLines)

            CommissionWidget.updateCommission(commissionIndex, "$name: $progress")
        }

        override fun dataChanged(menu: AbstractContainerMenu, property: Int, value: Int) = Unit
    }

    private fun findCommissionName(lines: List<String>): String? {
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            for (type in CommissionFormat.COMMISSIONS) {
                if (trimmed.equals(type.name, ignoreCase = true)) {
                    return type.name
                }
            }
        }
        return null
    }

    private fun findProgress(lines: List<String>): String {
        if (lines.any { it.contains("COMPLETED", ignoreCase = true) }) return "DONE"

        val progressIdx = lines.indexOfFirst { it.contains("Progress", ignoreCase = true) }
        if (progressIdx != -1) {
            for (i in progressIdx..minOf(progressIdx + 2, lines.size - 1)) {
                val match = Regex("\\d+(?:\\.\\d+)?%").find(lines[i])
                if (match != null) return match.value
            }
        }
        return "0%"
    }

    fun initKeyGuards() {
        // Cancels vanilla hotbar swap while in the commissions container
        keybindCancelEvent()
    }

    fun onClientTick(client: Minecraft) {
        if (!HypixelUtils.isOnSkyblock || !ConfigAccess.isCommissionsKeybindsEnabled()) {
            detachListener()
            return
        }

        val screen = client.screen as? AbstractContainerScreen<*> ?: run {
            detachListener()
            return
        }

        val title = screen.title.string
        if (!title.contains("Commissions", ignoreCase = true)) {
            detachListener()
            return
        }

        attachListener(screen.menu)

        val now = System.currentTimeMillis()
        if (now - openedAt < CLICK_DEBOUNCE_MS) return
        if (now - lastClick < CLICK_DEBOUNCE_MS) return

        val pressedIndex = resolveCommissionIndex(client) ?: return

        val slotIndex = when (pressedIndex) {
            0 -> 11
            1 -> 12
            2 -> 14
            3 -> 15
            else -> return
        }

        if (slotIndex !in screen.menu.slots.indices) return
        val slot = screen.menu.getSlot(slotIndex)
        val clickedItem = slot.item.copy()
        if (!slot.hasItem()) return

        val player = client.player ?: return
        val gm = client.gameMode ?: return

        // Check if claimed commission is completed
        val clickedTooltipLines = clickedItem.getTooltipLines(
            Item.TooltipContext.of(client.level!!.registryAccess()),
            player,
            AbilityItemParser.tooltipFlag()
        ).map { it.string }

        if (clickedTooltipLines.any { it.contains("COMPLETED", ignoreCase = true) }) {
            CommissionsTracker.onCommissionClaimed()
        }

        gm.handleInventoryMouseClick(
            screen.menu.containerId,
            slot.index,
            0,
            ClickType.PICKUP,
            player
        )

        lastClick = now
    }

    private fun resolveCommissionIndex(client: Minecraft): Int? {
        for (i in keybinds.indices) {
            val keyCode = keybinds[i]
            if (keyCode == 0) continue

            val downNow = isKeyDown(client, keyCode)
            val downBefore = wasDown[keyCode] == true
            wasDown[keyCode] = downNow

            if (downNow && !downBefore) return i
        }
        return null
    }

    private fun isKeyDown(client: Minecraft, keyCode: Int): Boolean {
        if (keyCode == 0) return false
        val window = client.window.handle()

        return if (keyCode < 0) {
            val mouseButton = keyCode + 100
            GLFW.glfwGetMouseButton(window, mouseButton) == GLFW.GLFW_PRESS
        } else {
            GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS
        }
    }

    private fun attachListener(menu: AbstractContainerMenu) {
        if (attachedMenu === menu) return
        detachListener()
        attachedMenu = menu
        menu.addSlotListener(menuListener)
        wasDown.clear()
        openedAt = System.currentTimeMillis()
        lastClick = openedAt
    }

    private fun detachListener() {
        val m = attachedMenu ?: return
        m.removeSlotListener(menuListener)
        attachedMenu = null
        wasDown.clear()
    }

    private fun keybindCancelEvent() {
        if (keyGuardActive) return
        keyGuardActive = true

        ScreenEvents.AFTER_INIT.register(ScreenEvents.AfterInit { _, screen, _, _ ->
            if (!guardedScreens.add(screen)) return@AfterInit

            registerKeyGuards(screen)

            ScreenEvents.remove(screen).register(ScreenEvents.Remove { s ->
                guardedScreens.remove(s)
            })
        })
    }

    private fun registerKeyGuards(screen: Screen) {

        ScreenKeyboardEvents.allowKeyPress(screen).register(ScreenKeyboardEvents.AllowKeyPress { s, event ->
            val container = s as? AbstractContainerScreen<*> ?: return@AllowKeyPress true
            if (!shouldBlockNumberKeys(container)) return@AllowKeyPress true

            val keyCode = event.key
            keyCode !in GLFW.GLFW_KEY_1..GLFW.GLFW_KEY_9
        })

        ScreenKeyboardEvents.allowKeyRelease(screen).register(ScreenKeyboardEvents.AllowKeyRelease { s, event ->
            val container = s as? AbstractContainerScreen<*> ?: return@AllowKeyRelease true
            if (!shouldBlockNumberKeys(container)) return@AllowKeyRelease true

            val keyCode = event.key
            keyCode !in GLFW.GLFW_KEY_1..GLFW.GLFW_KEY_9
        })
    }

    private fun shouldBlockNumberKeys(screen: AbstractContainerScreen<*>): Boolean {
        if (!HypixelUtils.isOnSkyblock) return false
        if (!ConfigAccess.isCommissionsEnabled()) return false
        return screen.title.string.contains("Commissions", ignoreCase = true)
    }
}