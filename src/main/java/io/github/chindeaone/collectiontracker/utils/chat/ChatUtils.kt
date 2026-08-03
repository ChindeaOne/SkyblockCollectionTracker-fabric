package io.github.chindeaone.collectiontracker.utils.chat

import io.github.chindeaone.collectiontracker.utils.ColorUtils
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.client.multiplayer.chat.GuiMessageSource
import net.minecraft.client.multiplayer.chat.GuiMessageTag
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MessageSignature
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.net.URI
import kotlin.math.abs

object ChatUtils {

    private val logger: Logger = LogManager.getLogger(ChatUtils::class.java)
    private val PREFIX: Component = ColorUtils.getPrefixComponent()

    private const val COMMAND_PAGE_MESSAGE_ID = 1
    private const val CATEGORY_PAGE_MESSAGE_ID = 2
    private val messageSignatures = mutableMapOf<Int, MessageSignature>()

    @JvmStatic
    fun sendMessage(message: String, prefix: Boolean = true) {
        val messageComponent = Component.literal(message)
        val text = if (prefix) {
            Component.empty().append(PREFIX).append(messageComponent)
        } else {
            messageComponent
        }
        Minecraft.getInstance().gui/*? if 26.2 {*/ /*.hud *//*?}*/.chat.addClientSystemMessage(text)
    }

    private fun sendEmptyMessage() {
        sendMessage("", prefix = false)
    }

    fun sendComponent(component: Component, prefix: Boolean = true, messageId: Int? = null) {
        val finalComponent = if (prefix) {
            Component.empty().append(PREFIX).append(component)
        } else {
            component
        }

        val chat = Minecraft.getInstance().gui/*? if 26.2 {*/ /*.hud *//*?}*/.chat

        if (messageId == null) {
            chat.addClientSystemMessage(finalComponent)
        } else {
            val signature = createMessageSignature(messageId)
            removeChatMessage(chat, signature)
            chat.addMessage(finalComponent, signature, GuiMessageSource.SYSTEM_CLIENT, GuiMessageTag.system())
        }
    }

    private fun createMessageSignature(id: Int): MessageSignature {
        val key = abs(id).mod(255 * 128)

        return messageSignatures.getOrPut(key) {
            val data = ByteArray(256)

            val fullBytes = key / 128
            data.fill(127, 0, fullBytes)

            data[fullBytes] = (key % 128).toByte()

            MessageSignature(data)
        }
    }

    private fun removeChatMessage(chat: ChatComponent, signature: MessageSignature) {
        val message = chat.allMessages.firstOrNull { it.signature == signature } ?: return
        chat.allMessages.remove(message)

        if (chat.trimmedMessages.removeIf { it.parent === message }) {
            return
        }

        chat.refreshTrimmedMessages()
    }

    fun sendCommandComponent(
        text: String,
        command: String,
        prefix: Boolean = true
    ) {
        val component = Component.literal(text)
            .withStyle { style: Style? ->
                (style ?: Style.EMPTY).withClickEvent(ClickEvent.RunCommand(command))
            }

        sendComponent(component, prefix)
    }

    fun sendClickableLinkComponent(
        text: String,
        hover: String,
        url: String?
    ) {
        if (url == null) {
            logger.info("[SCT]: Error: URL is null. Cannot create clickable link.")
            return
        }

        val clickableComponent = Component.literal(text)
            .withStyle { style: Style? ->
                (style ?: Style.EMPTY).withClickEvent(ClickEvent.OpenUrl(URI.create(url)))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal(hover)))
            }

        sendComponent(clickableComponent, prefix = true)
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

    private fun getWidth(): Int = ChatComponent.getWidth(Minecraft.getInstance().options.chatWidth().get())

    @JvmStatic
    fun sendCommandPage(
        category: String,
        color: String,
        commands: List<MutableComponent>,
        page: Int,
        totalPages: Int
    ) {
        val divider = fillChat()
        val title: Component = buildCommandTitleBar(page, totalPages).centerText()
        val categoryTitle: Component = Component.literal("$color§l$category").centerText()

        val message = Component.empty()

        fun appendLine(component: Component) {
            if (!message.string.isEmpty()) {
                message.append(Component.literal("\n"))
            }
            message.append(component)
        }

        appendLine(Component.empty())
        appendLine(divider)
        appendLine(title)
        appendLine(divider)
        appendLine(categoryTitle)

        commands.forEach(::appendLine)

        appendLine(Component.empty())
        appendLine(divider)
        appendLine(Component.empty())

        sendComponent(message, prefix = false, messageId = COMMAND_PAGE_MESSAGE_ID)
    }

    private fun buildCommandTitleBar(page: Int, totalPages: Int): Component {
        val title: MutableComponent = Component.literal("")

        if (page > 1) {
            title.append(
                Component.literal("§6<< ")
                    .withStyle {
                        it.withClickEvent(ClickEvent.RunCommand("/sct commands ${page - 1}"))
                            .withHoverEvent(HoverEvent.ShowText(Component.literal("§7Previous page")))
                    }
            )
        } else {
            title.append(Component.literal("§7<< "))
        }

        title.append(Component.literal("§6§lSkyblockCollectionTracker §7- §eCommands §7($page/$totalPages)"))

        if (page < totalPages) {
            title.append(
                Component.literal(" §6>>")
                    .withStyle {
                        it.withClickEvent(ClickEvent.RunCommand("/sct commands ${page + 1}"))
                            .withHoverEvent(HoverEvent.ShowText(Component.literal("§7Next page")))
                    }
            )
        } else {
            title.append(Component.literal(" §7>>"))
        }
        return title
    }

    @JvmStatic
    fun sendSummary(
        title: String,
        lines: List<Component>
    ) {
        val divider = fillChat()
        val displayTitle = title.asComponent().centerText()

        sendEmptyMessage()
        sendComponent(divider, prefix = false)
        sendComponent(displayTitle, prefix = false)
        sendEmptyMessage()

        for (line in lines) sendComponent(line, prefix = false)

        sendEmptyMessage()
        sendComponent(divider, prefix = false)
        sendEmptyMessage()
    }

    @JvmStatic
    fun sendCategoryPage(
        category: String,
        color: String,
        collections: MutableList<String>,
        page: Int,
        totalPages: Int
    ) {
        val divider = fillChat()
        val title: Component = buildTitleBar(page, totalPages).centerText()
        val collectionTitle: Component = Component.literal("$color§l$category Collections").centerText()

        val message = Component.empty()

        fun appendLine(component: Component) {
            if (message.string.isNotEmpty()) {
                message.append(Component.literal("\n"))
            }
            message.append(component)
        }

        appendLine(divider)
        appendLine(title)
        appendLine(divider)
        appendLine(collectionTitle)

        collections.forEach { collection ->
            appendLine(
                Component.literal("   $color- $collection")
                    .withStyle {
                        it.withClickEvent(ClickEvent.RunCommand("/sct track $collection"))
                            .withHoverEvent(
                                HoverEvent.ShowText(
                                    Component.literal("§eClick to track the $color$collection§e collection!")
                                )
                            )
                    }
            )
        }
        appendLine(divider)

        sendComponent(message, prefix = false, messageId = CATEGORY_PAGE_MESSAGE_ID)
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

        title.append(Component.literal("§6§lSkyblockCollectionTracker §7- §eCollections §7($page/$totalPages)"))

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