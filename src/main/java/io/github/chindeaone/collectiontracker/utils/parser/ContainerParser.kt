package io.github.chindeaone.collectiontracker.utils.parser

import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.HypixelUtils
import io.github.chindeaone.collectiontracker.utils.StringUtils.removeColor
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.currentBeekeeperBuff
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.currentLotteryBuff
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.currentSkyMallBuff
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener.isPickaxeAbility
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.Identifier
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

    fun onScreenChanged(screen: Screen?) {
        if (!HypixelUtils.isInSkyblock) return

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
        if (!HypixelUtils.isInSkyblock) return

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
        findCoreStack(screen).let { stack ->
            if (ConfigAccess.getCotmLevel() == 10) return@let

            val level = getTooltips(stack, client).getOrNull(1)?.split(" ")?.getOrNull(1)?.toIntOrNull() ?: return@let
            if (level != ConfigAccess.getCotmLevel()) {
                ConfigHelper.setCotmLevel(level)
            }
        }

        findBuffsStack(screen, "Sky Mall")?.let { stack ->
            val itemModel = stack.get(DataComponents.ITEM_MODEL)

            if (itemModel == Identifier.withDefaultNamespace("coal") && ConfigAccess.isSkyMallEnabled()) {
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

            val result = parseEffect(stack, client) ?: return@let

            currentSkyMallBuff = result
            isPickaxeAbility = result.contains("cooldown", ignoreCase = true)
        }

        findPerksStack(screen, "Professional")?.let { stack ->
            val level = getTooltips(stack, client)
                .getOrNull(1)?.split(" ")
                ?.getOrNull(1)?.split("/")
                ?.getOrNull(0)?.toIntOrNull()
                ?: return@let

            if (level != ConfigAccess.getProfessionalMS()) {
                ConfigHelper.setProfessionalMS(level)
            }
        }

        findPerksStack(screen, "Strong Arm")?.let { stack ->
            val level = getTooltips(stack, client)
                .getOrNull(1)?.split(" ")
                ?.getOrNull(1)?.split("/")
                ?.getOrNull(0)?.toIntOrNull()
                ?: return@let

            if (level != ConfigAccess.getStrongArmMS()) {
                ConfigHelper.setStrongArmMS(level)
            }
        }

        currentHotxScreen = null
        currentHotxType = null
    }

    private fun handleHotf(screen: AbstractContainerScreen<*>, client: Minecraft) {
        findCoreStack(screen).let { stack ->
            if (ConfigAccess.getCotfLevel() == 5) return@let

            val level = getTooltips(stack, client).getOrNull(1)?.split(" ")?.getOrNull(1)?.toIntOrNull() ?: return@let
            if (level != ConfigAccess.getCotfLevel()) {
                ConfigHelper.setCotfLevel(level)
            }
        }

        findBuffsStack(screen, "Lottery")?.let { stack ->
            val itemModel = stack.get(DataComponents.ITEM_MODEL)

            if (itemModel == Identifier.withDefaultNamespace("pale_oak_button") && ConfigAccess.isLotteryEnabled()) {
                currentLotteryBuff = ""
                ConfigHelper.setLastLotteryBuff(currentLotteryBuff)
                return@let
            }

            if (itemModel == Identifier.withDefaultNamespace("stripped_mangrove_log")) {
                currentLotteryBuff = "§cDisabled"
                ConfigHelper.setLastLotteryBuff(currentLotteryBuff)
                return@let
            }

            parseEffect(stack, client)?.let { effect ->
                currentLotteryBuff = effect
            }
        }

        screen.menu.getSlot(7).item.let { beekeeperStack ->
            if (beekeeperStack.isEmpty) return@let

            val itemModel = beekeeperStack.get(DataComponents.ITEM_MODEL)

            if (itemModel == Identifier.withDefaultNamespace("pale_oak_button") && ConfigAccess.isBeekeeperEnabled()) {
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
                parseEffect(beekeeperStack, client)?.let { effect ->
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

    private fun parseEffect(stack: ItemStack, client: Minecraft): String? {
        val tooltip = getTooltips(stack, client)

        return tooltip
            .dropWhile { it != "Your Current Effect" }
            .drop(1)
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?.let(ChatListener::compactBuffs)
    }

    private fun getTooltips(stack: ItemStack, client: Minecraft): List<String> {
        val level = client.level ?: return emptyList()

        return stack.getTooltipLines(
            Item.TooltipContext.of(level.registryAccess()),
            client.player,
            AbilityItemParser.tooltipFlag()
        ).map { it.string }
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