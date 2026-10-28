package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.hotx.HotfData
import io.github.chindeaone.collectiontracker.utils.hotx.HotmData
import io.github.chindeaone.collectiontracker.utils.inventory.ItemUtils
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import java.util.HashMap

object ContainerParser {

    var currentCommissionScreen: AbstractContainerScreen<*>? = null
    var attachedCommissionMenu: AbstractContainerMenu? = null
    var isCommissionMenuOpen = false

    var openedAt: EpochMark? = null
    var lastClick: EpochMark? = null
    val wasDown = HashMap<Int, Boolean>()

    val pattern: Regex = Regex("""^\((\d+)/(\d+)\) Loadouts$""")

    enum class ScreenType {
        HOTM,
        HOTF,
        HOTM_SLOTS,
        HOTF_SLOTS,
        LOADOUTS
    }

    private var currentScreenType: ScreenType? = null
    private var currentScreen: AbstractContainerScreen<*>? = null

    fun onScreenChanged(screen: Screen?) {
        currentCommissionScreen = null
        detachListener()

        val container = screen as? AbstractContainerScreen<*> ?: return
        onCommissionScreen(container)
        onHotxScreen(container)
        onLoadoutsScreen(container)
    }

    private fun onHotxScreen(container: AbstractContainerScreen<*>) {
        val title = container.title.string
        when (title) {
            "Heart of the Mountain" -> {
                currentScreenType = ScreenType.HOTM
            }
            "Heart of the Mountain Slot" -> {
                currentScreenType = ScreenType.HOTM_SLOTS
            }
            "Heart of the Forest" -> {
                currentScreenType = ScreenType.HOTF
            }
            "Heart of the Forest Slot" -> {
                currentScreenType = ScreenType.HOTF_SLOTS
            }
        }
        currentScreen = container
    }

    private fun onLoadoutsScreen(container: AbstractContainerScreen<*>) {
        if (container.title.string.matches(pattern)) {
            currentScreenType = ScreenType.LOADOUTS // Fixed from currentScreenType
            currentScreen = container
        }
    }

    fun onClientTick(client: Minecraft) {
        val screen = currentScreen ?: return

        if (client./*? if 26.2 {*/ /*gui.screen() *//*?} else {*/ screen /*?}*/ !== screen) {
            currentScreen = null
            currentScreenType = null
            return
        }

        when (currentScreenType) {
            ScreenType.HOTM -> handleHotm(screen)
            ScreenType.HOTF -> handleHotf(screen)
            ScreenType.HOTM_SLOTS -> handleHotmSlots(screen)
            ScreenType.HOTF_SLOTS -> handleHotxSlots(screen)
            ScreenType.LOADOUTS -> handleLoadouts(screen)
            null -> return
        }

        resetScreen()
    }

    private fun handleHotm(screen: AbstractContainerScreen<*>) {
        HotmData.update(screen)
    }

    private fun handleHotf(screen: AbstractContainerScreen<*>) {
        HotfData.update(screen)
    }

    private fun handleHotmSlots(screen: AbstractContainerScreen<*>) {
        HotmData.updateSlots(screen)
    }

    private fun handleHotxSlots(screen: AbstractContainerScreen<*>) {
        HotfData.updateSlots(screen)
    }

    private fun handleLoadouts(screen: AbstractContainerScreen<*>) {
        HotfData.updateCurrentLoadout(screen, slotIndex = 9)
        HotmData.updateCurrentLoadout(screen, slotIndex = 18)
    }

    fun parseCurrentLoadoutName(stack: ItemStack): String? {
        if (stack.isEmpty) return null
        val lines = ItemUtils.getTooltipLines(stack)
        val currentLine = lines.firstOrNull { it.contains("Current: ") } ?: return null
        return currentLine.substringAfter("Current: ").trim().ifBlank { null }
    }

    fun visibleSlots(screen: AbstractContainerScreen<*>): List<ItemStack> {
        val menu = screen.menu
        val result = mutableListOf<ItemStack>()

        for (row in 0..4) {
            for (col in 1..7) {
                val idx = row * 9 + col
                if (idx >= menu.slots.size) continue
                result += menu.getSlot(idx).item
            }
        }
        return result
    }

    private fun onCommissionScreen(container: AbstractContainerScreen<*>) {
        if (container.title.string != "Commissions") return

        currentCommissionScreen = container
        isCommissionMenuOpen = true
        attachListener(container.menu)

        ScreenEvents.remove(container).register {
            detachListener()
        }
    }

    private fun attachListener(menu: AbstractContainerMenu) {
        if (attachedCommissionMenu === menu) return

        attachedCommissionMenu = menu
        wasDown.clear()

        openedAt = EpochMark.now()
        lastClick = openedAt
    }

    private fun detachListener() {
        if (attachedCommissionMenu == null) return

        attachedCommissionMenu = null
        wasDown.clear()
        isCommissionMenuOpen = false
    }

    private fun resetScreen() {
        currentScreen = null
        currentScreenType = null
    }
}