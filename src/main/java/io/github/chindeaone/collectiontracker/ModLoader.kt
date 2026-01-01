/*
* This kotlin object is derived from the SkyHanni mod.
*/
package io.github.chindeaone.collectiontracker

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker.modules
import io.github.chindeaone.collectiontracker.commands.CommandRegistry
import io.github.chindeaone.collectiontracker.gui.overlays.CollectionOverlay
import io.github.chindeaone.collectiontracker.util.CommissionsKeybinds
import io.github.chindeaone.collectiontracker.util.Hypixel
import io.github.chindeaone.collectiontracker.util.ServerUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation

class ModLoader: ModInitializer {

    override fun onInitialize() {
        eventRegistration()

        SkyblockCollectionTracker.preInit()
        SkyblockCollectionTracker.init()
        loadedClasses.clear()

        CommandRegistry.init()
    }

    private fun eventRegistration() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {client ->
            if (client.player == null) return@EndTick
            TickDispatcher.onEndClientTick(client)
        })
        ClientPlayConnectionEvents.DISCONNECT.register{ _, _ -> Hypixel.onDisconnect() }

        val overlayId = ResourceLocation.fromNamespaceAndPath(SkyblockCollectionTracker.MODID, "overlay")
        HudElementRegistry.attachElementBefore(VanillaHudElements.SLEEP, overlayId) { context, tickCounter ->
            CollectionOverlay.render(context, tickCounter)
        }
    }

    private object TickDispatcher {
        fun onEndClientTick(client: Minecraft) {
            // Call every onTick here
            SkyblockCollectionTracker.onTick(client)
            ServerUtils.onClientTick(client)
            Hypixel.onTick(client)
            CommissionsKeybinds.onClientTick(client)
        }
    }

    companion object {
        private val loadedClasses = mutableSetOf<String>()

        fun loadModule(obj: Any) {
            if (!loadedClasses.add(obj.javaClass.name)) throw IllegalStateException("Module ${obj.javaClass.name} is already loaded")
            modules.add(obj)
        }
    }
}