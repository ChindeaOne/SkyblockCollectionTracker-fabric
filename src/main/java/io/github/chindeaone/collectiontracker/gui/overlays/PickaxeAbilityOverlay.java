package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.TextUtils;
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker;
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PickaxeAbilityOverlay implements AbstractOverlay{

    private final Position position = ConfigAccess.getPickaxeAbilityPosition();
    private final List<String> pickaxeAbilityOverlayLines = new ArrayList<>();
    private boolean renderingAllowed  = true;
    private boolean expiredTitleShown = true;
    private boolean readyTitleShown = true;

    @Override
    public String overlayLabel() {
        return "Pickaxe Ability";
    }

    @Override
    public Position position() {
        return position;
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
        renderingAllowed = allowed;
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
        if (ConfigAccess.isPickaxeAbilityInMiningIslandsOnly() && !MiningMapping.INSTANCE.getMiningIslands().contains(IslandTracker.getCurrentMiningIsland())) return Collections.emptyList();

        String abilityName = ConfigAccess.getPickaxeAbilityName();
        String displayName = abilityName.isEmpty() ? "Unknown Ability" : abilityName;

        double cooldown = ChatListener.getFinalCooldown();
        double active = ChatListener.getFinalDuration();

        if (active > 0) {
            expiredTitleShown = false;
            readyTitleShown = false;
        }

        if (ConfigAccess.isColeweightAbilityFormat()) {
            // Title logic for Coleweight format
            if (active == 0) {
                if (ConfigAccess.isShowPickaxeExpiredAbilityTitle() && !expiredTitleShown && cooldown > 0 && !displayName.equals("Pickobulus")) {
                    String titleExpired = "§6[§3§kd§6] §b§l" + displayName + " §cExpired! §6[§3§kd§6]";
                    RenderUtils.showTitle(Component.literal(titleExpired), ConfigAccess.getTitleDisplayTimer());
                    expiredTitleShown = true;
                }
                if (cooldown <= 0) {
                    if (ConfigAccess.isShowPickaxeReadyAbilityTitle() && !readyTitleShown) {
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
            if (!ConfigAccess.isAbilityCooldownOnly() && active > 0) {
                status = "§a" + TextUtils.formatTime(active);
            } else if (cooldown > 0) {
                status = "§c" + TextUtils.formatTime(cooldown);
            } else {
                status = "§aReady!";
            }
            pickaxeAbilityOverlayLines.add("§e" + displayName + " CD: " + status);
        } else {
            // Original Title logic
            if (active == 0) {
                if (ConfigAccess.isShowPickaxeExpiredAbilityTitle() && !expiredTitleShown && cooldown > 0 && !displayName.equals("Pickobulus")) {
                    RenderUtils.showTitle(Component.literal("§6" + displayName + " §cExpired!"), ConfigAccess.getTitleDisplayTimer());
                    expiredTitleShown = true;
                }
                if (cooldown <= 0) {
                    if (ConfigAccess.isShowPickaxeReadyAbilityTitle() && !readyTitleShown) {
                        RenderUtils.showTitle(Component.literal("§6" + displayName + " §aReady!"), ConfigAccess.getTitleDisplayTimer());
                        readyTitleShown = true;
                    }
                } else {
                    readyTitleShown = false;
                }
            }

            // Original format lines
            if (displayName.equals("Unknown Ability")) displayName = "§cUnknown Ability";
            else displayName = "§e" + displayName;
            pickaxeAbilityOverlayLines.add("§bPickaxe Ability: " + displayName);

            if (!ConfigAccess.isAbilityCooldownOnly() && active > 0) {
                pickaxeAbilityOverlayLines.add("§aActive: " + TextUtils.formatTime(active));
            } else if (cooldown > 0) {
                pickaxeAbilityOverlayLines.add("§cCooldown: " + TextUtils.formatTime(cooldown));
            } else {
                pickaxeAbilityOverlayLines.add("§aReady!");
            }
        }
        return pickaxeAbilityOverlayLines;
    }
}
