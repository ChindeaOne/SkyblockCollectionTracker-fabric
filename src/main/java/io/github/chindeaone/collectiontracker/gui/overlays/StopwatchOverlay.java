package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class StopwatchOverlay extends AbstractOverlay{

    private final Position position = ConfigAccess.getColeweightStopwatchPosition();
    private final List<String> stopwatchLines = new ArrayList<>();
    private long stopwatchStart = 0L;
    private long stopwatchElapsed = 0L;
    private boolean stopwatchRunning = false;
    private boolean stopwatchPaused = false;

    @Override
    public String overlayLabel() {
        return "Stopwatch Overlay";
    }

    @Override
    public Position position() {
        return position;
    }

    @Override
    public boolean isEnabled() {
        return stopwatchRunning  && HypixelUtils.isOnSkyblock();
    }

    @Override
    public void render(GuiGraphics context) {
        if (!isEnabled()) return;

        List<String> lines = getStopwatchLines();
        if (lines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
                RenderUtils.renderStrings(context, lines));
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getStopwatchLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    public void startStopwatch() {
        if (stopwatchRunning && !stopwatchPaused) {
            ChatUtils.sendMessage("§cStopwatch is already running!", true);
            return;
        }

        stopwatchStart = System.currentTimeMillis();
        stopwatchElapsed = 0L;
        stopwatchRunning = true;
        stopwatchPaused = false;

        ChatUtils.sendMessage("§aStopwatch started!", true);
    }

    public void stopStopwatch() {
        if (!stopwatchRunning) {
            ChatUtils.sendMessage("§cStopwatch is not running!", true);
            return;
        }

        long elapsed = stopwatchPaused
                ? stopwatchElapsed
                : stopwatchElapsed + (System.currentTimeMillis() - stopwatchStart);

        ChatUtils.sendMessage("§cStopwatch stopped at §e" + formatStopwatchTime(elapsed) + "§c!", true);

        stopwatchStart = 0L;
        stopwatchElapsed = 0L;
        stopwatchRunning = false;
        stopwatchPaused = false;
    }

    public void pauseStopwatch() {
        if (!stopwatchRunning) {
            ChatUtils.sendMessage("§cStopwatch is not running!", true);
            return;
        }

        if (!stopwatchPaused) {
            stopwatchElapsed += System.currentTimeMillis() - stopwatchStart;
            stopwatchPaused = true;
            ChatUtils.sendMessage("§eStopwatch paused!", true);
        } else {
            stopwatchStart = System.currentTimeMillis();
            stopwatchPaused = false;
            ChatUtils.sendMessage("§aStopwatch resumed!", true);
        }
    }

    private List<String> getStopwatchLines() {
        stopwatchLines.clear();

        if (!stopwatchRunning) return stopwatchLines;

        long elapsed = stopwatchPaused
                ? stopwatchElapsed
                : stopwatchElapsed + (System.currentTimeMillis() - stopwatchStart);

        String pauseText = stopwatchPaused ? "§7 (Paused)" : "";

        stopwatchLines.add("§bStopwatch: §e" + formatStopwatchTime(elapsed) + pauseText);

        return stopwatchLines;
    }

    private String formatStopwatchTime(long elapsedMillis) {
        int hours = (int) (elapsedMillis / 3600000);
        int minutes = (int) ((elapsedMillis % 3600000) / 60000);
        int seconds = (int) ((elapsedMillis % 60000) / 1000);

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
