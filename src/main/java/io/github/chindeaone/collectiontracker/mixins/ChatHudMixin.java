package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.util.chat.ChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatHudMixin {
    @ModifyVariable(
            method = "addMessage*",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Component modifyVisualMessage(Component message) {
        return ChatListener.INSTANCE.coleweightHandle(message);
    }
}