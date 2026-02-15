package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.event.ServerTickCallback
import net.minecraft.network.protocol.common.ClientboundPingPacket

object ServerTickUtils {

    private var lastId = -1
    private val listeners = mutableListOf<ServerTickCallback>()

    fun register(listener: ServerTickCallback) {
        listeners.add(listener)
    }

    @JvmStatic
    fun onServerTick(packet: ClientboundPingPacket) {
        if (packet.id != lastId) {
            lastId = packet.id
            listeners.forEach { it.onTick() }
        }
    }

    fun reset() {
        lastId = -1
    }
}