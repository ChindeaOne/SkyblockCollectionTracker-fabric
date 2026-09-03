package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.ServerTickUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
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
        if (!HypixelUtils.isInSkyblock()) return;
       ChatListener.skillListener(packet.content().toString());
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)
    private void sct$onHandleSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if (!HypixelUtils.isInSkyblock()) return;
        if (ChatListener.dailyPerksUpdate(packet.content())) {
            ci.cancel();
        }
    }
}
