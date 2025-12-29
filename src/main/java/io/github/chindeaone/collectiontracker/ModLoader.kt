/*
* This kotlin object is derived from the SkyHanni mod.
*/
package io.github.chindeaone.collectiontracker

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker.modules
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

class ModLoader: ModInitializer {

    override fun onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register {it.tick()}
        SkyblockCollectionTracker.preInit()
        SkyblockCollectionTracker.init()
        loadedClasses.clear()
    }

    companion object {
        private val loadedClasses = mutableSetOf<String>()

        fun loadModule(obj: Any) {
            if (!loadedClasses.add(obj.javaClass.name)) throw IllegalStateException("Module ${obj.javaClass.name} is already loaded")
            modules.add(obj)
        }
    }
}