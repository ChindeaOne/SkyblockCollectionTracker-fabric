package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatComponent.class)
public class ChatHudMixin {
    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Component modifyVisualMessage(Component message) {
        return ChatListener.coleweightHandle(message);
    }

    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddMessage(Component message, CallbackInfo ci) {
        if (ChatListener.dailyPerksUpdate(message)) {
            ci.cancel();
        }
    }
}