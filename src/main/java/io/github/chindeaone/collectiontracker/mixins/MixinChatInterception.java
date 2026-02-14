package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientPacketListener.class, priority = 999) // Higher priority for Skyhanni autopet hider
public class MixinChatInterception {
    @Inject(
            method = "handleSystemChat",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onHandleSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        String text = packet.content().getString();
        ChatListener.petSwapListener(text);
        if (ChatListener.dailyPerksUpdate(packet.content())) {
            ci.cancel();
        }
    }
}
