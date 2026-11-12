package io.github.chindeaone.collectiontracker.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

object MinecraftUtils {

    private val mc = Minecraft.getInstance()

    val user get() = mc.user

    val name get() = user.name

    val level get() = mc.level

    val player get() = mc.player

    val gameMode get() = mc.gameMode

    val services get() = mc.services()

    val gui get() = mc.gui.hud

    val window get() = mc.window

    val mouseHandler get() = mc.mouseHandler

    val tabList get() = mc.gui.hud.tabList

    val chat get() = gui.chat

    val options get() = mc.options

    val isSameThread get() = mc.isSameThread

    val profileId get() = user.profileId

    val playerSkinRenderCache get() = mc.playerSkinRenderCache()

    val entityRenderDispatcher get() = mc.entityRenderDispatcher

    val accessToken get() = user.accessToken

    val font get() = mc.font

    val hideGui get() = mc.gui.hud.isHidden

    val isDebugHudVisible get() = mc.debugEntries.isOverlayVisible

    val hitResult get() = mc.hitResult

    val connection get() = mc.connection

    val gameRenderer get() = mc.gameRenderer

    val deltaTracker get() = mc.deltaTracker

    val soundManager get() = mc.soundManager

    val screen get() = mc.gui.screen()

    fun isChatScreenOpen(): Boolean {
        return screen is ChatScreen
    }

    fun setScreen(screen: AbstractContainerScreen<*>?) {
        mc.gui.setScreen(screen)
    }

    fun setScreen(screen: Screen) {
        mc.gui.setScreen(screen)
    }

    fun runOnClientThread(action: () -> Unit) {
        if (isSameThread) {
            action()
        } else {
            mc.execute(action)
        }
    }
}