package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.cotfLevel
import io.github.chindeaone.collectiontracker.config.cotmLevel
import io.github.chindeaone.collectiontracker.config.enableBeekeeper
import io.github.chindeaone.collectiontracker.config.enableLottery
import io.github.chindeaone.collectiontracker.config.enableSkyMall
import io.github.chindeaone.collectiontracker.config.professionalMS
import io.github.chindeaone.collectiontracker.config.strongArmMS
import io.github.chindeaone.collectiontracker.utils.EpochMark
import io.github.chindeaone.collectiontracker.utils.StringUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.currentBeekeeperBuff
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.currentLotteryBuff
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.currentSkyMallBuff
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.isPickaxeAbility
import io.github.chindeaone.collectiontracker.utils.inventory.ItemUtils
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player
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

    private enum class HotxScreenType {
        HOTM,
        HOTF
    }

    private var currentHotxScreen: AbstractContainerScreen<*>? = null
    private var currentHotxType: HotxScreenType? = null

    fun onScreenChanged(screen: Screen?) {
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
        val player = client.player ?: return
        val screen = currentHotxScreen ?: return

        if (client./*? if 26.2 {*/ /*gui.screen() *//*?} else {*/ screen /*?}*/ !== screen) {
            currentHotxScreen = null
            currentHotxType = null
            return
        }

        when (currentHotxType) {
            HotxScreenType.HOTM -> handleHotm(screen, player)
            HotxScreenType.HOTF -> handleHotf(screen, player)
            null -> return
        }
    }

    private fun handleHotm(screen: AbstractContainerScreen<*>, player: Player) {
        findCoreStack(screen).let { stack ->
            val cotmLevel = cotmLevel
            if (cotmLevel == 10) return@let

            val tooltipLines = ItemUtils.getTooltipLines(stack, player)

            val level = tooltipLines.getOrNull(1)?.split(" ")?.getOrNull(1)?.toIntOrNull() ?: return@let
            if (level != cotmLevel) {
                ConfigHelper.setCotmLevel(level)
            }
        }

        findBuffsStack(screen, "Sky Mall")?.let { stack ->
            val itemModel = stack.get(DataComponents.ITEM_MODEL)

            if (itemModel == Identifier.withDefaultNamespace("coal") && enableSkyMall) {
                currentSkyMallBuff = ""
                ConfigHelper.setLastSkyMallBuff(currentSkyMallBuff)
                isPickaxeAbility = false
                return@let
            }

            if (itemModel == Identifier.withDefaultNamespace("redstone_block")) {
                currentSkyMallBuff = "§cDisabled"
                ConfigHelper.setLastSkyMallBuff(currentSkyMallBuff)
                isPickaxeAbility = false
                return@let
            }

            val result = parseEffect(stack, player) ?: return@let

            currentSkyMallBuff = result
            isPickaxeAbility = result.contains("cooldown", ignoreCase = true)
        }

        findPerksStack(screen, "Professional")?.let { stack ->
            val tooltipLines = ItemUtils.getTooltipLines(stack, player)

            val level = tooltipLines
                .getOrNull(1)?.split(" ")
                ?.getOrNull(1)?.split("/")
                ?.getOrNull(0)?.toIntOrNull()
                ?: return@let

            if (level != professionalMS) {
                ConfigHelper.setProfessionalMS(level)
            }
        }

        findPerksStack(screen, "Strong Arm")?.let { stack ->
            val tooltipLines = ItemUtils.getTooltipLines(stack, player)

            val level = tooltipLines
                .getOrNull(1)?.split(" ")
                ?.getOrNull(1)?.split("/")
                ?.getOrNull(0)?.toIntOrNull()
                ?: return@let

            if (level != strongArmMS) {
                ConfigHelper.setStrongArmMS(level)
            }
        }

        currentHotxScreen = null
        currentHotxType = null
    }

    private fun handleHotf(screen: AbstractContainerScreen<*>, player: Player) {
        findCoreStack(screen).let { stack ->
            val cotfLevel = cotfLevel
            if (cotfLevel == 5) return@let

            val tooltipLines = ItemUtils.getTooltipLines(stack, player)

            val level = tooltipLines.getOrNull(1)?.split(" ")?.getOrNull(1)?.toIntOrNull() ?: return@let
            if (level != cotfLevel) {
                ConfigHelper.setCotfLevel(level)
            }
        }

        findBuffsStack(screen, "Lottery")?.let { stack ->
            val itemModel = stack.get(DataComponents.ITEM_MODEL)

            if (itemModel == Identifier.withDefaultNamespace("pale_oak_button") && enableLottery) {
                currentLotteryBuff = ""
                ConfigHelper.setLastLotteryBuff(currentLotteryBuff)
                return@let
            }

            if (itemModel == Identifier.withDefaultNamespace("stripped_mangrove_log")) {
                currentLotteryBuff = "§cDisabled"
                ConfigHelper.setLastLotteryBuff(currentLotteryBuff)
                return@let
            }

            parseEffect(stack, player)?.let { effect ->
                currentLotteryBuff = effect
            }
        }

        screen.menu.getSlot(7).item.let { beekeeperStack ->
            if (beekeeperStack.isEmpty) return@let

            val itemModel = beekeeperStack.get(DataComponents.ITEM_MODEL)

            if (itemModel == Identifier.withDefaultNamespace("pale_oak_button") && enableBeekeeper) {
                currentBeekeeperBuff = ""
                ConfigHelper.setLastBeekeeperBuff(currentBeekeeperBuff)
                return@let
            }

            if (itemModel == Identifier.withDefaultNamespace("stripped_mangrove_log")) {
                currentBeekeeperBuff = "§cDisabled"
                ConfigHelper.setLastBeekeeperBuff(currentBeekeeperBuff)
                return@let
            }

            if (beekeeperStack.hoverName.string.removeColor() == "Beekeeper") {
                parseEffect(beekeeperStack, player)?.let { effect ->
                    currentBeekeeperBuff = effect
                }
            }
        }

        currentHotxScreen = null
        currentHotxType = null
    }

    private fun findBuffsStack(screen: AbstractContainerScreen<*>, name: String): ItemStack? {
        for (slot in 10..37 step 9) {
            val stack = screen.menu.getSlot(slot).item
            if (!stack.isEmpty && stack.hoverName.string.removeColor() == name) {
                return stack
            }
        }
        return null
    }

    private fun findCoreStack(screen: AbstractContainerScreen<*>): ItemStack = screen.menu.getSlot(4).item

    private fun findPerksStack(screen: AbstractContainerScreen<*>, name: String): ItemStack? {
        for (slot in 3..39 step 9) {
            val stack = screen.menu.getSlot(slot).item
            if (!stack.isEmpty && stack.hoverName.string.removeColor() == name) {
                return stack
            }
        }
        return null
    }

    private fun parseEffect(stack: ItemStack, player: Player): String? {
        val tooltipLines = ItemUtils.getTooltipLines(stack, player)

        return tooltipLines
            .dropWhile { it != "Your Current Effect" }
            .drop(1)
            .takeWhile { it.isNotBlank()}
            .joinToString(" ")
            .trim()
            .takeIf { it.isNotEmpty() }
            ?.let(StringUtils::formatHotXPerkBuffs)
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

        openedAt = EpochMark.now()
        lastClick = openedAt
    }

    private fun detachListener() {
        if (attachedCommissionMenu == null) return

        attachedCommissionMenu = null
        wasDown.clear()
        isCommissionMenuOpen = false
    }
}