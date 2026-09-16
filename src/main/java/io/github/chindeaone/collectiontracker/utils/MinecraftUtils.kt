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

    val gui get() = mc.gui /*? if 26.2 {*/ /*.hud *//*?}*/

    val tabList get() = mc.gui /*? if < 26.2 {*/ .tabList /*?} else {*/ /*.hud.tabList *//*?}*/

    val chat get() = gui /*? if < 26.3 {*/ .chat  /*?} else {*/ /*.hud.chat *//*?}*/

    val options get() = mc.options

    val isSameThread get() = mc.isSameThread

    val profileId get() = user.profileId

    val accessToken get() = user.accessToken

    val font get() = mc.font

    val hideGui get() = mc./*? if > 26.1 {*/ /*gui.hud.isHidden  *//*?} else {*/options.hideGui /*?}*/
    
    val isDebugHudVisible get() = mc.debugEntries.isOverlayVisible

    val screen get() = mc./*? if > 26.1 {*/ /*gui.screen() *//*?} else {*/ screen /*?}*/

    fun isChatScreenOpen(): Boolean {
        return screen is ChatScreen
    }

    fun setScreen(screen: AbstractContainerScreen<*>?) {
        mc./*? if > 26.1 {*/ /*gui.setScreen *//*?} else {*/ setScreen /*?}*/(screen)
    }

    fun setScreen(screen: Screen) {
        mc./*? if > 26.1 {*/ /*gui.setScreen *//*?} else {*/ setScreen /*?}*/(screen)
    }

    fun runOnClientThread(action: () -> Unit) {
        if (isSameThread) {
            action()
        } else {
            mc.execute(action)
        }
    }
}