package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.CommissionFormat;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.utils.tab.CommissionsWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommissionsOverlay implements AbstractOverlay{

    private final Position position = ConfigAccess.getCommissionsPosition();
    private final List<String> formattedCommissions = new ArrayList<>();
    private boolean renderingAllowed  = true;

    private List<String> getCommissionsLines() {
        List<String> raw = CommissionsWidget.INSTANCE.getRawCommissions();
        if (raw.isEmpty()) return Collections.emptyList();

        formattedCommissions.clear();
        CommissionFormat.Area detectedArea = null;

        for (String line : raw) {
            String formatted = line;
            String lowerLine = line.toLowerCase();
            for (CommissionFormat.CommissionType type : CommissionFormat.INSTANCE.getCOMMISSIONS()) {
                String typeNameLower = type.getName().toLowerCase();
                if (lowerLine.contains(typeNameLower)) {
                    formatted = type.getFormat().invoke(line);
                    if (detectedArea == null) detectedArea = type.getArea();
                    break;
                }
            }
            formattedCommissions.add(formatted);
        }

        if (detectedArea != null) {
            switch (detectedArea) {
                case DWARVEN_MINES -> formattedCommissions.addFirst("§2§l" + detectedArea.getDisplayName());
                case CRYSTAL_HOLLOWS -> formattedCommissions.addFirst("§5§l" + detectedArea.getDisplayName());
                case GLACITE_TUNNELS -> formattedCommissions.addFirst("§b§l" + detectedArea.getDisplayName());
            }
        }
        return formattedCommissions;
    }

    @Override
    public String overlayLabel() {
        return "Commissions Overlay";
    }

    @Override
    public Position position() {
        return this.position;
    }

    @Override
    public boolean isEnabled() {
        return ConfigAccess.isCommissionsEnabled() && HypixelUtils.isOnSkyblock();
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
        List<String> lines = getCommissionsLines();

        if (lines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
            RenderUtils.renderStrings(context, lines)
        );
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getCommissionsLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }
}