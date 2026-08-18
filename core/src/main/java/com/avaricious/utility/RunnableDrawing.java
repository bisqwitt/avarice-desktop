package com.avaricious.utility;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RunnableDrawing implements Drawing {

    private final ZIndex layer;
    private final Runnable runnable;

    private Color color;

    public RunnableDrawing(Runnable runnable, ZIndex layer) {
        this.runnable = runnable;
        this.layer = layer;
    }

    public RunnableDrawing(Runnable runnable, ZIndex layer, Color color) {
        this.runnable = runnable;
        this.layer = layer;
        this.color = color;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (color != null) batch.setColor(color);
        runnable.run();
        if (color != null) batch.setColor(1f, 1f, 1f, 1f);
    }

    @Override
    public ZIndex getZIndex() {
        return layer;
    }
}
