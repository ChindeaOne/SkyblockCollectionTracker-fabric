package io.github.chindeaone.collectiontracker.config.core;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.utils.rendering.ScaleUtils;

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
        int screenWidth = ScaleUtils.INSTANCE.getScaledWidth();
        int screenHeight = ScaleUtils.INSTANCE.getScaledHeight();

        int yPadding = 4;
        int scaledYPadding = Math.round(yPadding * this.scale);

        int scaledWidth = Math.round(this.width * this.scale);
        int scaledHeight = Math.round(this.height * this.scale);

        int maxX = screenWidth - scaledWidth;
        int maxY = screenHeight - (scaledHeight + scaledYPadding);

        if (maxX < 0) maxX = 0;
        if (maxY < scaledYPadding) maxY = scaledYPadding;

        this.X = Math.clamp(x, 0, maxX);
        this.Y = Math.clamp(y, scaledYPadding, maxY);
    }

    public void setScaling(float scale) {
        this.scale = scale;
        setPosition(this.X, this.Y);
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        setPosition(this.X, this.Y);
    }
}
