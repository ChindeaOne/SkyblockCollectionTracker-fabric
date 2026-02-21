package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.TextUtils;
import io.github.chindeaone.collectiontracker.utils.tab.ForagingStatsWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AxeAbilityOverlay implements AbstractOverlay{

    private final Position position = ConfigAccess.getAxeAbilityPosition();
    private final List<String> axeAbilityOverlayLines = new ArrayList<>();
    private boolean renderingAllowed  = true;
    private boolean expiredTitleShown = true;
    private boolean readyTitleShown = true;

    @Override
    public String overlayLabel() {
        return "Axe Ability";
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
        if (ConfigAccess.isAxeAbilityInForagingIslandsOnly() && ForagingStatsWidget.getCurrentForagingIsland() == null) return Collections.emptyList();

        String abilityName = ConfigAccess.getAxeAbilityName();
        String displayName = abilityName.isEmpty() ? "Unknown Ability" : abilityName;

        double cooldown = ChatListener.getFinalAxeCooldown();
        double duration = ChatListener.getFinalAxeDuration();

        if (duration > 0) {
            expiredTitleShown = false;
            readyTitleShown = false;
        }

        if (ConfigAccess.isColeweightAbilityFormat()) {
            // Title logic for Coleweight format
            if (ConfigAccess.isShowAxeAbilityTitle() && duration == 0) {
                if (!expiredTitleShown && cooldown > 0) {
                    String titleExpired = "§6[§3§kd§6] §b§l" + displayName + " §cExpired! §6[§3§kd§6]";
                    RenderUtils.showTitle(Component.literal(titleExpired), ConfigAccess.getTitleDisplayTimer());
                    expiredTitleShown = true;
                }
                if (cooldown == 0) {
                    if (!readyTitleShown) {
                        String titleReady = "§6[§3§kd§6] §b§l" + displayName + " §6[§3§kd§6]";
                        RenderUtils.showTitle(Component.literal(titleReady), ConfigAccess.getTitleDisplayTimer());
                        readyTitleShown = true;
                    }
                } else {
                    readyTitleShown = false;
                }
            }

            // Lines logic for Coleweight format
            String status;
            if (!ConfigAccess.isAbilityCooldownOnly() && duration > 0) {
                status = "§a" + TextUtils.formatTime(duration);
            } else if (cooldown > 0) {
                status = "§c" + TextUtils.formatTime(cooldown);
            } else {
                status = "§aReady!";
            }
            axeAbilityOverlayLines.add("§e" + displayName + " CD: " + status);
        } else {
            // Original Title logic
            if (ConfigAccess.isShowAxeAbilityTitle() && duration == 0) {
                if (!expiredTitleShown && cooldown > 0) {
                    RenderUtils.showTitle(Component.literal("§6" + displayName + " §cExpired!"), ConfigAccess.getTitleDisplayTimer());
                    expiredTitleShown = true;
                }
                if (cooldown == 0) {
                    if (!readyTitleShown) {
                        RenderUtils.showTitle(Component.literal("§6" + displayName + " §aReady!"), ConfigAccess.getTitleDisplayTimer());
                        readyTitleShown = true;
                    }
                } else {
                    readyTitleShown = false;
                }
            }

            // Original format lines
            if (abilityName.isEmpty()) {
                axeAbilityOverlayLines.add("§cUnknown Ability");
            } else {
                axeAbilityOverlayLines.add("§bAxe Ability: §e" + abilityName);
            }

            if (!ConfigAccess.isAbilityCooldownOnly() && duration > 0) {
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