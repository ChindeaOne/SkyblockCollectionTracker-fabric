package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.ServerTickUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
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
}
