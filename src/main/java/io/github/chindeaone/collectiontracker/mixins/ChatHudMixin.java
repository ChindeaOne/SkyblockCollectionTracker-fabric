package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
}