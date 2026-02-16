package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.ServerTickUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPacketListener.class) // Higher priority for Skyhanni
public class ClientPacketListenerMixin {

    @Inject(method = "handleSetTime", at = @At("RETURN"))
    private void sct$onServerTick(ClientboundSetTimePacket packet, CallbackInfo ci) {
        long gameTime = packet.gameTime();
        Minecraft.getInstance().execute(() -> ServerTickUtils.onServerTick(gameTime));
    }

    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void sct$onLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        Minecraft.getInstance().execute(ServerTickUtils::reset);
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    private void sct$onSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        ChatListener.skillListener(packet.content().toString());
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)
    private void onHandleSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        String text = packet.content().getString();
        ChatListener.petSwapListener(text);
        if (ChatListener.dailyPerksUpdate(packet.content())) {
            ci.cancel();
        }
    }
}
