/*
* This kotlin class is derived from the SkyHanni mod.
*/
package io.github.chindeaone.collectiontracker

import io.github.chindeaone.collectiontracker.commands.CommandRegistry
import io.github.chindeaone.collectiontracker.gui.OverlayManager
import io.github.chindeaone.collectiontracker.utils.CommissionsKeybinds
import io.github.chindeaone.collectiontracker.utils.Hypixel
import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils
import io.github.chindeaone.collectiontracker.utils.ServerUtils
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import io.github.chindeaone.collectiontracker.utils.inventory.InventoryListener
import io.github.chindeaone.collectiontracker.utils.tab.TabData
import io.github.chindeaone.collectiontracker.utils.world.BlockWatcher
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.client.Minecraft
//? if = 1.21.11 {
import net.minecraft.resources.Identifier
//? } else {
 /*import net.minecraft.resources.ResourceLocation 
*///? }

class ModLoader: ModInitializer {

    override fun onInitialize() {
        SkyblockCollectionTracker.init()

        OverlayManager.overlayRegistration()
        eventRegistration()

        CommandRegistry.init()

        CommissionsKeybinds.initKeyGuards()
    }


    private fun eventRegistration() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {client ->
            if (client.player == null) return@EndTick
            TickDispatcher.onEndClientTick(client)
        })
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> Hypixel.onDisconnect() }
        ClientReceiveMessageEvents.GAME.register { message, _ -> ChatListener.onChatMessage(message) }

        UseItemCallback.EVENT.register { player, _, hand -> ChatListener.checkHandItem(player, hand) }

        //? if = 1.21.11 {
        val overlayId = Identifier.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "overlay")
        //? } else {
         /*val overlayId = ResourceLocation.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "overlay") 
        *///? }
        HudElementRegistry.attachElementBefore(VanillaHudElements.SLEEP, overlayId) { context, _ ->
            // Avoid rendering in editor mode
            if(OverlayManager.isInEditorMode()) return@attachElementBefore

            for (overlay in OverlayManager.all()) {
                if (overlay.shouldRender()) {
                    overlay.render(context)
                }
            }
        }
    }

    private object TickDispatcher {
        fun onEndClientTick(client: Minecraft) {
            // Call every onTick here
            SkyblockCollectionTracker.onTick(client)
            ServerUtils.onTick(client)
            Hypixel.onTick(client)
            CommissionsKeybinds.onClientTick(client)
            TabData.tickAndUpdateWidget(client)
            BlockWatcher.onClientTick(client)
            ScoreboardUtils.onTick(client)
            InventoryListener.onTick(client)
        }
    }
}