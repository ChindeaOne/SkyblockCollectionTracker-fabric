package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.util.HypixelUtils;
import io.github.chindeaone.collectiontracker.util.chat.ChatListener;
import io.github.chindeaone.collectiontracker.util.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PickaxeAbilityOverlay implements AbstractOverlay{

    private final Position position = ConfigAccess.getPickaxeAbilityPosition();
    private final List<String> pickaxeAbilityOverlayLines = new ArrayList<>();
    private boolean renderingAllowed  = true;

    @Override
    public String overlayLabel() {
        return "Pickaxe Ability Overlay";
    }

    @Override
    public Position position() {
        return this.position;
    }

    @Override
    public boolean isEnabled() {
        return ConfigAccess.isPickaxeAbilityDisplayed() && HypixelUtils.isOnSkyblock();
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
        List<String> lines = getPickaxeAbilityLines();

        if (lines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
                RenderUtils.renderStrings(context, lines)
        );
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getPickaxeAbilityLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    private List<String> getPickaxeAbilityLines() {
        pickaxeAbilityOverlayLines.clear();
        if (!ConfigAccess.isPickaxeAbilityDisplayed()) return Collections.emptyList();

        if (ConfigAccess.getPickaxeAbilityName().isEmpty()) {
            pickaxeAbilityOverlayLines.add("§cUnknown Ability");
        } else {
            pickaxeAbilityOverlayLines.add("§bAbility: §e" + ConfigAccess.getPickaxeAbilityName());
        }
        long now = System.currentTimeMillis();
        double cooldown = (ChatListener.getFinalCooldown() - now) / 1000.0;
        double duration = (ChatListener.getFinalDuration() - now) / 1000.0;

        if (duration >= 0) {
            // Ability active
            pickaxeAbilityOverlayLines.add(String.format("§aActive: %.1fs", duration));
        } else if (cooldown > 0) {
            // Ability on cooldown
            pickaxeAbilityOverlayLines.add(String.format("§cCooldown: %.1fs",cooldown));
        } else {
            // Ability ready
            pickaxeAbilityOverlayLines.add("§aReady!");
        }
        return pickaxeAbilityOverlayLines;
    }
}
