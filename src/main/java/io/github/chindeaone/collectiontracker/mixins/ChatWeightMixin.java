package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.StringUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ChatComponent.class, priority = 1001) // Lower priority for Skyhanni
public class ChatWeightMixin {
    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Component modifyVisualMessage(Component message) {
        if (StringUtils.INSTANCE.removeColor(message.getString(), false).startsWith("[SCT]")) return message;
        return ChatListener.farmingweightHandle(ChatListener.coleweightHandle(message));
    }
}