package io.github.chindeaone.collectiontracker.util

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
//? if = 1.21.11 {
import net.minecraft.client.gui.components.ChatComponent
//? }
import net.minecraft.network.chat.*

object ChatUtils {

    private const val SCT = "§6SCT§r"
    private const val PREFIX = "§o§3[${SCT}§3] §r"

    fun sendMessage(message: String, prefix: Boolean = true) {
        val text = if (prefix) "$PREFIX$message" else message
        Minecraft.getInstance().player?.displayClientMessage(Component.literal(text), false)
    }

    fun sendEmptyMessage() {
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

    private fun Component.centerText(width: Int = getWidth()): Component {
        val textWidth = Minecraft.getInstance().font.width(this)
        val spaceWidth = Minecraft.getInstance().font.width(Component.literal(" "))
        val paddingPixels = (width - textWidth) / 2
        val spaces = " ".repeat((paddingPixels / spaceWidth).coerceAtLeast(0))
        if (spaces.isEmpty()) return this

        return Component.empty().apply {
            append(spaces.asComponent())
            append(this@centerText)
        }
    }

    private fun fillChat(symbol: String = "-", style: ChatFormatting = ChatFormatting.GOLD, width: Int = getWidth()): Component {
        val symbolComponent = Component.literal(symbol).withStyle(style, ChatFormatting.STRIKETHROUGH)
        val symbolWidth = Minecraft.getInstance().font.width(symbolComponent)
        if (symbolWidth <= 0) return symbolComponent
        if (symbolWidth >= width) return symbolComponent
        val repeat = (width / symbolWidth).coerceAtLeast(1)
        val component = Component.literal("")
        repeat(repeat) { component.append(symbolComponent) }
        return component
    }

    private fun getWidth(): Int {
        //? if = 1.21.11 {
        return ChatComponent.getWidth(Minecraft.getInstance().options.chatWidth().get())
        //? } else {
         /*return Minecraft.getInstance().gui.chat.width 
        *///? }
    }

    fun sendCommands(
        title: String,
        commands: List<MutableComponent>
    ) {
        val divider = fillChat()
        val displayTitle = title.asComponent().centerText()

        sendEmptyMessage()
        sendComponent(divider, prefix = false)
        sendComponent(displayTitle, prefix = false)
        sendComponent(divider, prefix = false)
        sendEmptyMessage()

        for (command in commands) sendComponent(command, prefix = false)

        sendEmptyMessage()
        sendComponent(divider, prefix = false)
        sendEmptyMessage()
    }

    fun sendSummary(
        title: String,
        lines: List<String>
    ) {
        val divider = fillChat()
        val displayTitle = title.asComponent().centerText()

        sendEmptyMessage()
        sendComponent(divider, prefix = false)
        sendComponent(displayTitle, prefix = false)
        sendEmptyMessage()

        for (line in lines) sendComponent(line.asComponent(), prefix = false)

        sendEmptyMessage()
        sendComponent(divider, prefix = false)
        sendEmptyMessage()
    }

    fun sendCategoryPage(
        category: String,
        color: String,
        collections: MutableList<String>,
        page: Int,
        totalPages: Int
    ) {
        val divider = fillChat()
        val title: Component = buildTitleBar(page, totalPages).centerText()

        sendComponent(divider, prefix = false)
        sendComponent(title, prefix = false)
        sendComponent(divider, prefix = false)

        val collectionTitle: Component = Component.literal("$color§l$category Collections").centerText()
        sendComponent(collectionTitle, prefix = false)

        for (collection in collections) {
            val message = Component.literal("   $color- $collection")
                .withStyle { style: Style? ->
                    style!!
                        .withClickEvent(ClickEvent.RunCommand("/sct track $collection"))
                        .withHoverEvent(
                            HoverEvent.ShowText(
                                Component.literal("§eClick to track the $color$collection§e collection!")
                            )
                        )
                }
            sendComponent(message, prefix = false)
        }
        sendComponent(divider, prefix = false)
    }

    private fun buildTitleBar(page: Int, totalPages: Int): Component {
        val title: MutableComponent = Component.literal("")

        if (page > 1) {
            title.append(
                Component.literal("§6<< ")
                    .withStyle {
                        it.withClickEvent(ClickEvent.RunCommand("/sct collections ${page - 1}"))
                            .withHoverEvent(HoverEvent.ShowText(Component.literal("§7Previous category")))
                    }
            )
        } else {
            title.append(Component.literal("§7<< "))
        }

        title.append(Component.literal("§6§lSkyblockCollectionTracker Collections §7($page/$totalPages)"))

        if (page < totalPages) {
            title.append(
                Component.literal(" §6>>")
                    .withStyle {
                        it.withClickEvent(ClickEvent.RunCommand("/sct collections ${page+1}"))
                          .withHoverEvent(HoverEvent.ShowText(Component.literal("§7Next category")) )
                    }
            )
        } else {
            title.append(Component.literal(" §7>>"))
        }
        return title
    }
}