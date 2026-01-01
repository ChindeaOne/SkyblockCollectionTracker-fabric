package io.github.chindeaone.collectiontracker.config.core;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.util.rendering.ScaleUtils;

public class Position {

    @Expose
    private int X;
    @Expose
    private int Y;
    @Expose
    private float scale = 1.0f;
    @Expose
    private int width = 100;
    @Expose
    private int height = 20;

    public Position(int x, int y) {
        this.X = x;
        this.Y = y;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public float getScale() {
        return scale;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setPosition(int x, int y) {
        int screenWidth = ScaleUtils.INSTANCE.getWidth();
        int screenHeight = ScaleUtils.INSTANCE.getHeight();

        int scaledWidth = Math.round(width * scale);
        int scaledHeight = Math.round(height * scale);

        int maxX = screenWidth - scaledWidth;
        int maxY = screenHeight - scaledHeight;

        if (maxX < 0) maxX = 0;
        if (maxY < 0) maxY = 0;

        this.X = Math.max(0, Math.min(x, maxX));
        this.Y = Math.max(0, Math.min(y, maxY));
    }

    public void setScaling(float scale) {
        this.scale = scale;
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
