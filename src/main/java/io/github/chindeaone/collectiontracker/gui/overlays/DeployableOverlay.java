package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.parser.DeployableParser;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class DeployableOverlay implements AbstractOverlay {

    private final Position position = ConfigAccess.getDeployablePosition();
    private final List<String> deployableLines = new ArrayList<>();
    private boolean renderingAllowed = true;

    @Override
    public String overlayLabel() {
        return "Mining Deployable Overlay";
    }

    @Override
    public Position position() {
        return this.position;
    }

    @Override
    public boolean isEnabled() {
        return ConfigAccess.isDeployableEnabled() && HypixelUtils.isOnSkyblock();
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
        List<String> lines = getDeployableLines();

        if (lines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
                RenderUtils.renderStrings(context, lines)
        );
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getDeployableLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    private List<String> getDeployableLines() {
        deployableLines.clear();

        String buff = DeployableParser.getBuff();
        String expireTime = DeployableParser.getRemainingTime();

        if (buff.isEmpty() || expireTime.isEmpty() || !DeployableParser.isNear()) {
            return deployableLines;
        }

        int timeLeft = 0;
        try {
            timeLeft = Integer.parseInt(expireTime.replace("s", ""));
        } catch (NumberFormatException ignored) {}

        String buffColor = DeployableParser.getBuffColor();
        if (timeLeft <= 5) {
            deployableLines.add(buffColor + buff + " §cSoon!");
        } else {
            deployableLines.add(buffColor + buff + " §e" + timeLeft + "s");
        }
        return deployableLines;
    }
}