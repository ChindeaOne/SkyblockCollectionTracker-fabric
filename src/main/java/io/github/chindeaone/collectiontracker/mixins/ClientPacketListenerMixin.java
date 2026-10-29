package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.MinecraftUtils;
import io.github.chindeaone.collectiontracker.utils.ServerTickUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import io.github.chindeaone.collectiontracker.utils.inventory.InventoryListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleSetTime", at = @At("RETURN"))
    private void sct$onServerTick(ClientboundSetTimePacket packet, CallbackInfo ci) {
        if (!HypixelUtils.isInSkyblock()) return;
        ServerTickUtils.onServerTick(packet.gameTime());
    }

    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void sct$onLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        ServerTickUtils.reset();
    }

    @Inject(method = "handleSystemChat", at = @At("RETURN"))
    private void sct$onSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        ChatListener.skillListener(packet.content().toString());
    }

    @Inject(method = "handleTakeItemEntity", at = @At("HEAD"))
    private void sct$onTakeItemEntity(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        if (!HypixelUtils.isInSkyblock()) return;
        if (!MinecraftUtils.INSTANCE.isSameThread()) return;

        InventoryListener.onItemPickup(packet.getItemId(), packet.getPlayerId());
    }
}
