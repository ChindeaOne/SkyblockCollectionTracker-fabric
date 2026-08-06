package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.HashMap

object ContainerParser {

    var currentCommissionScreen: AbstractContainerScreen<*>? = null
    var attachedCommissionMenu: AbstractContainerMenu? = null
    var isCommissionMenuOpen = false
        private set
    var openedAt = 0L

    var lastClick = -1L
    val wasDown = HashMap<Int, Boolean>()

    private enum class HotxScreenType {
        HOTM,
        HOTF
    }

    private var currentHotxScreen: AbstractContainerScreen<*>? = null
    private var currentHotxType: HotxScreenType? = null
    private var isSkyMallEnabled = false
    private var isLotteryEnabled = false
    private var isBeekeeperEnabled = false

    fun onScreenChanged(screen: Screen?) {
        if (!HypixelUtils.isOnSkyblock) return

        currentCommissionScreen = null
        detachListener()

        val container = screen as? AbstractContainerScreen<*> ?: return
        onCommissionScreen(container)
        onHotxScreen(container)
    }

    private fun onHotxScreen(container: AbstractContainerScreen<*>) {
        when {
            container.title.string.contains("Heart of the Mountain", ignoreCase = true) -> {
                currentHotxScreen = container
                currentHotxType = HotxScreenType.HOTM
            }

            container.title.string.contains("Heart of the Forest", ignoreCase = true) -> {
                currentHotxScreen = container
                currentHotxType = HotxScreenType.HOTF
            }
        }
    }

    fun onClientTick(client: Minecraft) {
        if (!HypixelUtils.isOnSkyblock) return

        val screen = currentHotxScreen ?: return

        if (client./*? if 26.2 {*/ /*gui.screen() *//*?} else {*/ screen /*?}*/ !== screen) {
            currentHotxScreen = null
            currentHotxType = null
            return
        }

        when (currentHotxType) {
            HotxScreenType.HOTM -> handleHotm(screen, client)
            HotxScreenType.HOTF -> handleHotf(screen, client)
            null -> return
        }
    }

    private fun handleHotm(screen: AbstractContainerScreen<*>, client: Minecraft) {
        val stack = findStack(screen, "Sky Mall") ?: return

        val result = parseEffect(stack, client) ?: return
        val (enabled, effect) = result

        if (!enabled) {
            isSkyMallEnabled = false
            return
        }

        isSkyMallEnabled = true
        ChatListener.currentSkyMallBuff = effect
        ChatListener.isPickaxeAbility = effect.contains("cooldown", ignoreCase = true)

        currentHotxScreen = null
        currentHotxType = null
    }

    private fun handleHotf(screen: AbstractContainerScreen<*>, client: Minecraft) {
        findStack(screen, "Lottery")?.let { stack ->
            parseEffect(stack, client)?.let { (enabled, effect) ->
                if (enabled) {
                    isLotteryEnabled = true
                    ChatListener.currentLotteryBuff = effect
                } else {
                    isLotteryEnabled = false
                }
            }
        }

        val beekeeper = screen.menu.getSlot(7).item
        if (!beekeeper.isEmpty && beekeeper.hoverName.string == "Beekeeper") {
            parseEffect(beekeeper, client)?.let { (enabled, effect) ->
                if (enabled) {
                    isBeekeeperEnabled = true
                    ChatListener.currentBeekeeperBuff = effect
                } else {
                    isBeekeeperEnabled = false
                }
            }
        }

        currentHotxScreen = null
        currentHotxType = null
    }

    private fun findStack(screen: AbstractContainerScreen<*>, name: String): ItemStack? {
        for (slot in 10..37 step 9) {
            val stack = screen.menu.getSlot(slot).item
            if (!stack.isEmpty && stack.hoverName.string == name) {
                return stack
            }
        }
        return null
    }

    private fun parseEffect(stack: ItemStack, client: Minecraft): Pair<Boolean, String>? {
        val level = client.level ?: return null

        val tooltip = stack.getTooltipLines(
            Item.TooltipContext.of(level.registryAccess()),
            client.player,
            AbilityItemParser.tooltipFlag()
        ).map { it.string }

        val enabled = when {
            tooltip.contains("ENABLED") -> true
            tooltip.contains("DISABLED") -> false
            else -> return null
        }

        val effect = tooltip
            .dropWhile { it != "Your Current Effect" }
            .drop(1)
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?.let(ChatListener::compactBuffs)
            ?: return null

        return enabled to effect
    }

    private fun onCommissionScreen(container: AbstractContainerScreen<*>) {
        if (!container.title.string.contains("Commissions", ignoreCase = true)) return

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

        openedAt = System.currentTimeMillis()
        lastClick = openedAt
    }

    private fun detachListener() {
        if (attachedCommissionMenu == null) return

        attachedCommissionMenu = null
        wasDown.clear()
        isCommissionMenuOpen = false
    }
}