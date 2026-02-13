package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.ConfigHelper;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler;
import io.github.chindeaone.collectiontracker.utils.ChatUtils;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

import static io.github.chindeaone.collectiontracker.commands.SkillTracker.skillName;
import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingRates.*;
import static io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatNumber;
import static io.github.chindeaone.collectiontracker.utils.rendering.TextUtils.formatNumberOrPlaceholder;

public class SkillOverlay implements AbstractOverlay {

    private final Position position = ConfigAccess.getSkillPosition();
    private final List<String> skillOverlayLines = new ArrayList<>();
    private final List<String> tamingOverlayLines = new ArrayList<>();
    private boolean renderingAllowed  = true;

    @Override
    public String overlayLabel() {
        return "Skill Overlay";
    }

    @Override
    public Position position() {
        return this.position;
    }

    @Override
    public boolean isEnabled() {
        return SkillTrackingHandler.isTracking && HypixelUtils.isOnSkyblock();
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

        List<String> mainLines = getSkillLines();
        List<String> tamingLines = getTamingLines();

        if (mainLines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
                RenderUtils.renderSkillStringsWithTaming(context, mainLines, tamingLines, ConfigAccess.isTamingTrackingEnabled()));
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getSkillLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    @SuppressWarnings("SameReturnValue")
    public List<String> getSkillLines() {
        skillOverlayLines.clear();

        skillOverlayLines.add(skillName + " Experience");
        skillOverlayLines.add(skillName + " Level: " + formatNumber(skillLevel));
        skillOverlayLines.add("Total " + skillName + " XP: " + formatNumberOrPlaceholder(totalSkillXp));
        skillOverlayLines.add("XP (Session): " + formatNumberOrPlaceholder(skillXpGained));
        skillOverlayLines.add("XP/h: " + formatNumberOrPlaceholder(skillPerHour));
        skillOverlayLines.add("Uptime: " + SkillTrackingHandler.getUptime());

        return skillOverlayLines;
    }

    public List<String> getTamingLines() {
        tamingOverlayLines.clear();

        if (skillName.equals("Taming")) {
            ConfigHelper.disableTamingTracking();
            return tamingOverlayLines;
        }

        if (tamingLevel == 0) {
            ConfigHelper.disableTamingTracking();
            ChatUtils.INSTANCE.sendMessage("§cCan't enable taming mid tracking. Enable this before tracking a skill!", true);
            return skillOverlayLines;
        }
        tamingOverlayLines.add("Taming Experience");
        tamingOverlayLines.add("Taming Level: " + formatNumber(tamingLevel));
        tamingOverlayLines.add("Total Taming XP: " + formatNumberOrPlaceholder(tamingXp));
        tamingOverlayLines.add("XP (Session): " + formatNumberOrPlaceholder(tamingXpGained));
        tamingOverlayLines.add("XP/h: " + formatNumberOrPlaceholder(tamingPerHour));

        return tamingOverlayLines;
    }
}