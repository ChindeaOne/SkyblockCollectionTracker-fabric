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

public class TimerOverlay implements AbstractOverlay{

    private final Position position = ConfigAccess.getColeweightTimerPosition();
    private final List<String> timerLines = new ArrayList<>();
    private long coleweightTimerEnd = 0;
    private long remainingTime = 0;
    private boolean isPaused = false;
    private boolean hasEnded = true;
    private boolean renderingAllowed = true;

    @Override
    public String overlayLabel() {
        return "Timer Overlay";
    }

    @Override
    public Position position() {
        return position;
    }

    @Override
    public boolean isEnabled() {
        return !hasEnded && HypixelUtils.isOnSkyblock();
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

        List<String> lines = getTimerLines();
        if (lines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
                RenderUtils.renderStrings(context, lines));
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getTimerLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    public void setTimer(int duration) {
        if (duration == 0) {
            ChatUtils.INSTANCE.sendMessage("§cTimer cancelled!", true);
            hasEnded = true;
            isPaused = false;
            return;
        }
        ChatUtils.INSTANCE.sendMessage("§aTimer set for " + duration + " seconds!", true);
        coleweightTimerEnd = System.currentTimeMillis() + duration * 1000L;
        isPaused = false;
        hasEnded = false;
    }

    public void pauseTimer() {
        if (hasEnded) {
            ChatUtils.INSTANCE.sendMessage("§cTimer has already ended!", true);
            return;
        }
        if (!isPaused && coleweightTimerEnd > System.currentTimeMillis()) {
            remainingTime = coleweightTimerEnd - System.currentTimeMillis();
            isPaused = true;
            ChatUtils.INSTANCE.sendMessage("§eTimer paused!", true);
        } else {
            coleweightTimerEnd = System.currentTimeMillis() + remainingTime;
            isPaused = false;
            ChatUtils.INSTANCE.sendMessage("§aTimer resumed!", true);
        }
    }

    private List<String> getTimerLines() {
        timerLines.clear();
        if (hasEnded) return timerLines;

        long now = System.currentTimeMillis();
        long remaining = isPaused? remainingTime : coleweightTimerEnd - now;

        if (remaining > 1000) {
            int hours = (int) (remaining / 3600000);
            int minutes = (int) ((remaining % 3600000) / 60000);
            int seconds = (int) ((remaining % 60000) / 1000);
            String pauseTarget = isPaused ? "§7 (Paused)" : "";

            String timeFormat;
            if (hours > 0) {
                timeFormat = String.format("%d:%02d:%02d", hours, minutes, seconds);
            } else if (minutes > 0) {
                timeFormat = String.format("%d:%02d", minutes, seconds);
            } else {
                timeFormat = String.format("%ds", seconds);
            }
            timerLines.add("§bTimer: §e" + timeFormat + pauseTarget);
        } else {
            ChatUtils.INSTANCE.sendMessage("§cTimer finished!", true);
            hasEnded = true;
        }

        return timerLines;
    }
}
