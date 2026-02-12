package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.util.HypixelUtils;
import io.github.chindeaone.collectiontracker.util.chat.ChatListener;
import io.github.chindeaone.collectiontracker.util.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.util.rendering.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AxeAbilityOverlay implements AbstractOverlay{

    private final Position position = ConfigAccess.getAxeAbilityPosition();
    private final List<String> axeAbilityOverlayLines = new ArrayList<>();
    private boolean renderingAllowed  = true;

    @Override
    public String overlayLabel() {
        return "Axe Ability Overlay";
    }

    @Override
    public Position position() {
        return this.position;
    }

    @Override
    public boolean isEnabled() {
        return ConfigAccess.isAxeAbilityDisplayed() && HypixelUtils.isOnSkyblock();
    }

    @Override
    public boolean isRenderingAllowed() {
        return renderingAllowed;
    }

    @Override
    public void setRenderingAllowed(boolean allowed) {
        this.renderingAllowed = allowed;
    }

    @Override
    public void render(GuiGraphics context) {
        if (!isEnabled()) return;
        List<String> lines = getAxeAbilityLines();

        if (lines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
                RenderUtils.renderStrings(context, lines)
        );
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getAxeAbilityLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    private List<String> getAxeAbilityLines() {
        axeAbilityOverlayLines.clear();
        if (!ConfigAccess.isAxeAbilityDisplayed()) return Collections.emptyList();

        String abilityName = ConfigAccess.getAxeAbilityName();
        String displayName = abilityName.isEmpty() ? "Unknown Ability" : abilityName;

        long now = System.currentTimeMillis();
        double cooldown = (ChatListener.getFinalAxeCooldown() - now) / 1000.0;
        double duration = (ChatListener.getFinalAxeDuration() - now) / 1000.0;

        if (ConfigAccess.isColeweightAbilityFormat()) {
            String status;
            if (duration >= 0) {
                status = "§a" + TextUtils.formatTime(duration);
            } else if (cooldown > 0) {
                status = "§c" + TextUtils.formatTime(cooldown);
            } else {
                status = "§aReady!";
            }
            axeAbilityOverlayLines.add("§e" + displayName + " CD: " + status);
        } else {
            // Original format
            if (abilityName.isEmpty()) {
                axeAbilityOverlayLines.add("§cUnknown Ability");
            } else {
                axeAbilityOverlayLines.add("§bPickaxe Ability: §e" + abilityName);
            }

            if (duration >= 0) {
                axeAbilityOverlayLines.add("§aActive: " + TextUtils.formatTime(duration));
            } else if (cooldown > 0) {
                axeAbilityOverlayLines.add("§cCooldown: " + TextUtils.formatTime(cooldown));
            } else {
                axeAbilityOverlayLines.add("§aReady!");
            }
        }
        return axeAbilityOverlayLines;
    }
}