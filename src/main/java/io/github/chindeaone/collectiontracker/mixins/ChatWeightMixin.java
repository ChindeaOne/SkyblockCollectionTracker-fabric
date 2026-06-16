package io.github.chindeaone.collectiontracker.mixins;

import io.github.chindeaone.collectiontracker.utils.StringUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatComponent.class, priority = 1001) // Lower priority for Skyhanni
public class ChatWeightMixin {
    @Inject(method = "addClientSystemMessage", at = @At("HEAD"))
    private void modifyVisualMessage(Component message, CallbackInfo ci) {
        if (!StringUtils.INSTANCE.removeColor(message.getString(), false).startsWith("[SCT]"))
            ChatListener.farmingweightHandle(ChatListener.coleweightHandle(message));
    }
}