package io.github.chindeaone.collectiontracker.util

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object ChatUtils {

    private const val SCT = "§6SCT§r"
    private const val PREFIX = "§o§3[${SCT}§3] §r"

    fun sendMessage(message: String, prefix: Boolean = true) {
        val text = if (prefix) "$PREFIX$message" else message
        Minecraft.getInstance().player?.displayClientMessage(Component.literal(text), false)
    }

    fun sendMessage() {
        sendMessage("", prefix = false)
    }

    fun sendComponent(component: Component, prefix: Boolean = true) {
        val finalComponent = if (prefix) {
            Component.literal(PREFIX).append(component)
        } else {
            component
        }
        Minecraft.getInstance().player?.displayClientMessage(finalComponent, false)
    }

    fun String.asComponent(): Component = Component.literal(this)

    private fun Component.centerText(width: Int = Minecraft.getInstance().gui.chat.width): Component {
        val textWidth = Minecraft.getInstance().font.width(this)
        val spaceWidth = Minecraft.getInstance().font.width(Component.literal(" "))
        val paddingPixels = (width - textWidth) / 2
        val spaces = " ".repeat((paddingPixels / spaceWidth).coerceAtLeast(0))
        if (spaces.isEmpty()) return this

        return Component.empty().apply {
            append(spaces.asComponent())
            append(this@centerText)
        }    }

    private fun fillChat(symbol: String = "-", style: ChatFormatting = ChatFormatting.GOLD, width: Int = Minecraft.getInstance().gui.chat.width): Component {
        val symbolComponent = Component.literal(symbol).withStyle(style, ChatFormatting.STRIKETHROUGH)
        val symbolWidth = Minecraft.getInstance().font.width(symbolComponent)
        if (symbolWidth <= 0) return symbolComponent
        if (symbolWidth >= width) return symbolComponent
        val repeat = (width / symbolWidth).coerceAtLeast(1)
        val component = Component.literal("")
        repeat(repeat) { component.append(symbolComponent) }
        return component
    }

    fun sendCommands(
        title: String,
        commands: List<MutableComponent>
    ) {
        val divider = fillChat()
        val displayTitle = title.asComponent().centerText()

        sendMessage()
        sendComponent(divider, prefix = false)
        sendComponent(displayTitle, prefix = false)
        sendComponent(divider, prefix = false)

        for (command in commands) sendComponent(command, prefix = false)

        sendComponent(divider, prefix = false)
        sendMessage()
    }

    fun sendSummary(
        title: String,
        lines: List<String>
    ) {
        val divider = fillChat()
        val displayTitle = title.asComponent().centerText()

        sendMessage()
        sendComponent(divider, prefix = false)
        sendComponent(displayTitle, prefix = false)
        sendMessage()

        for (line in lines) sendComponent(line.asComponent(), prefix = false)

        sendMessage()
        sendComponent(divider, prefix = false)
        sendMessage()
    }
}