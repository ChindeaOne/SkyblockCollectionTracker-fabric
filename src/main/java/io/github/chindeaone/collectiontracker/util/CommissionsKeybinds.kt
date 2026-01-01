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

package io.github.chindeaone.collectiontracker.util

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.ContainerListener
import net.minecraft.world.item.ItemStack
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.glfw.GLFW

object CommissionsKeybinds {

    var currentlyOpenChestName = ""
    private var lastClick = -1L
    private val keybinds: List<Int> get() = listOf(
        SkyblockCollectionTracker.configManager.config!!.mining.commissions.commission1,
        SkyblockCollectionTracker.configManager.config!!.mining.commissions.commission2,
        SkyblockCollectionTracker.configManager.config!!.mining.commissions.commission3,
        SkyblockCollectionTracker.configManager.config!!.mining.commissions.commission4,
    )
    private val logger: Logger = LogManager.getLogger(CommissionsKeybinds::class.java)

    private var attachedMenu: AbstractContainerMenu? = null
    private val wasDown = HashMap<Int, Boolean>()
    private const val CLICK_DEBOUNCE_MS = 200L

    private val menuListener = object : ContainerListener {
        override fun slotChanged(menu: AbstractContainerMenu, slotId: Int, stack: ItemStack) = Unit
        override fun dataChanged(menu: AbstractContainerMenu, property: Int, value: Int) = Unit
    }

    fun onClientTick(client: Minecraft) {
        if (!HypixelUtils.isOnSkyblock || !SkyblockCollectionTracker.configManager.config!!.mining.commissions.enableCommissionsKeybinds) {
            detachListener()
            return
        }

        val screen = client.screen as? AbstractContainerScreen<*>?: run {
            detachListener()
            return
        }

        attachListener(screen.menu)

        val title = screen.title.string
        currentlyOpenChestName = title

        if (!title.contains("Commissions", ignoreCase = true)) return

        val now = System.currentTimeMillis()
        if (now - lastClick < CLICK_DEBOUNCE_MS) return

        val slotToClick = resolveCommissionSlot(client) ?: return
        if (slotToClick !in 0 until screen.menu.slots.size) return

        val slot = screen.menu.getSlot(slotToClick)
        if (!slot.hasItem()) return

        val player = client.player ?: return
        val gm = client.gameMode ?: return

        gm.handleInventoryMouseClick(
            screen.menu.containerId,
            slotToClick,
            0,
            ClickType.PICKUP,
            player
        )

        lastClick = now
        logger.info("Clicked commissions slot $slotToClick: ${slot.item.displayName.string}")
    }

    private fun resolveCommissionSlot(client: Minecraft): Int? {
        for (i in keybinds.indices) {
            val keyCode = keybinds[i]
            if (keyCode == 0) continue

            val downNow = isKeyDown(client, keyCode)
            val downBefore = wasDown[keyCode] == true
            wasDown[keyCode] = downNow

            if (downNow && !downBefore) {
                return if (i < 2) i + 11 else i + 12
            }
        }
        return null
    }

    private fun isKeyDown(client: Minecraft, keyCode: Int): Boolean {
        if (keyCode == 0) return false

        val window = client.window.window
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
    }

    private fun detachListener() {
        val m = attachedMenu ?: return
        m.removeSlotListener(menuListener)
        attachedMenu = null
        wasDown.clear()
    }

}
