package io.github.chindeaone.collectiontracker.utils

import net.minecraft.client.Minecraft
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

    val chat get() = gui.chat

    val options get() = mc.options

    val isSameThread get() = mc.isSameThread

    val profileId get() = user.profileId

    val accessToken get() = user.accessToken

    val font get() = mc.font

    val hideGui get() = mc./*? if 26.2 {*/ /*gui.hud.isHidden() */ /*?} else {*/options.hideGui /*?}*/

    val isDebugHudVisible get() = mc.debugEntries.isOverlayVisible

    val screen get() = mc./*? if 26.2 {*/ /*gui.screen() *//*?} else {*/ screen /*?}*/

    fun setScreen(screen: AbstractContainerScreen<*>?) {
        mc./*? if 26.2 {*/ /*gui.setScreen *//*?} else {*/ setScreen /*?}*/(screen)
    }

    fun setScreen(screen: Screen) {
        mc./*? if 26.2 {*/ /*gui.setScreen *//*?} else {*/ setScreen /*?}*/(screen)
    }

    fun runOnClientThread(action: () -> Unit) {
        if (isSameThread) {
            action()
        } else {
            mc.execute(action)
        }
    }


}