package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.ConfigHelper;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler;
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

import static io.github.chindeaone.collectiontracker.commands.SkillTracker.skillName;
import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.leaderboardTrackingInitialized;
import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingRates.*;
import static io.github.chindeaone.collectiontracker.utils.NumbersUtils.formatNumber;
import static io.github.chindeaone.collectiontracker.utils.rendering.TextUtils.formatNumberOrPlaceholder;

public class SkillOverlay extends AbstractOverlay {

    private final Position position = ConfigAccess.getSkillPosition();
    private final List<String> skillOverlayLines = new ArrayList<>();
    private final List<String> tamingOverlayLines = new ArrayList<>();
    private boolean renderingAllowed  = true;

    private void addLeaderboardLines(List<String> list, int rank, String nextUser, long nextAmount, long tillNext, String eta) {
        if (!ConfigAccess.isSkillLeaderboardEnabled()) return;

        list.add("");

        if (rank == 1) return;

        if (nextUser != null) {
            list.add(String.format("Next Position (%s): %s", nextUser, formatNumber(nextAmount)));
            list.add("Till Next Position: " + formatNumber(tillNext));
            if (eta != null && !eta.isEmpty()) {
                list.add("ETA: " + eta);
            } else {
                list.add("ETA: Calculating...");
            }
        } else {
            list.add("Next Position: Calculating...");
            list.add("Till Next Position: Calculating...");
            list.add("ETA: Calculating...");
        }
    }

    @Override
    public String overlayLabel() {
        return "Skill Tracker";
    }

    @Override
    public Position position() {
        return position;
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
        renderingAllowed = allowed;
    }

    @Override
    public void render(GuiGraphicsExtractor context) {
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

        String rankSuffix = "";
        if (ConfigAccess.isSkillLeaderboardEnabled() && skillCurrentRank != -1) {
            rankSuffix = " [#" + skillCurrentRank + "]";
        }

        skillOverlayLines.add(skillName + " Level: " + formatNumber(skillLevel) + rankSuffix);
        skillOverlayLines.add("Total " + skillName + " XP: " + formatNumberOrPlaceholder(totalSkillXp));
        skillOverlayLines.add("XP (Session): " + formatNumberOrPlaceholder(skillXpGained));
        skillOverlayLines.add("XP/h: " + formatNumberOrPlaceholder(skillPerHour));

        if (ConfigAccess.isSkillLeaderboardEnabled() && !leaderboardTrackingInitialized) {
            ChatUtils.sendMessage("§cCan't enable skill leaderboard mid tracking. Enable this before tracking a skill!", true);
            ConfigHelper.disableSkillLeaderboardTracking();
        }

        addLeaderboardLines(skillOverlayLines, skillCurrentRank, skillNextRankUsername, skillNextRankAmount, skillTillNextRank, skillEtaToNextRank);

        skillOverlayLines.add("Uptime: " + SkillTrackingHandler.getUptime());

        return skillOverlayLines;
    }

    public List<String> getTamingLines() {
        tamingOverlayLines.clear();

        if (skillName.equals("Taming")) {
            ConfigHelper.disableTamingTracking();
            return tamingOverlayLines;
        }

        if (ConfigAccess.isTamingTrackingEnabled() && SkillTrackingHandler.getUptimeInSeconds() > 1 && tamingXp == 0) {
            ChatUtils.sendMessage("§cCan't enable taming mid tracking. Enable this before tracking a skill!", true);
            ConfigHelper.disableTamingTracking();
            return tamingOverlayLines;
        }

        if (!ConfigAccess.isTamingTrackingEnabled()) {
            return tamingOverlayLines;
        }

        String rankSuffix = "";
        if (ConfigAccess.isSkillLeaderboardEnabled() && tamingCurrentRank != -1) {
            rankSuffix = " [#" + tamingCurrentRank + "]";
        }

        tamingOverlayLines.add("Taming Level: " + formatNumber(tamingLevel) + rankSuffix);
        tamingOverlayLines.add("Total Taming XP: " + formatNumberOrPlaceholder(tamingXp + tamingXpGained));
        tamingOverlayLines.add("XP (Session): " + formatNumberOrPlaceholder(tamingXpGained));
        tamingOverlayLines.add("XP/h: " + formatNumberOrPlaceholder(tamingPerHour));

        addLeaderboardLines(tamingOverlayLines, tamingCurrentRank, tamingNextRankUsername, tamingNextRankAmount, tamingTillNextRank, tamingEtaToNextRank);

        return tamingOverlayLines;
    }
}