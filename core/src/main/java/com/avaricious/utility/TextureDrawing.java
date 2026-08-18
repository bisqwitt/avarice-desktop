package com.avaricious.utility;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class TextureDrawing implements Drawing {

    private final ZIndex layer;

    private final boolean additionalValues;
    private final TextureRegion textureRegion;
    private final float x;
    private final float y;
    private final float w;
    private final float h;

    private float scale;
    private float rotation;

    private Vector2 origin;

    private Runnable beforeDrawing;
    private Runnable afterDrawing;

    private Color color;

    public TextureDrawing(TextureRegion textureRegion, float x, float y, float w, float h, ZIndex layer) {
        this.textureRegion = textureRegion;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.layer = layer;
        additionalValues = false;
    }

    public TextureDrawing(TextureRegion textureRegion, float x, float y, float w, float h, ZIndex layer, Color color) {
        this.textureRegion = textureRegion;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.layer = layer;
        this.color = color;
        additionalValues = false;
    }

    public TextureDrawing(TextureRegion textureRegion, float x, float y, float w, float h, float scale, float rotation, ZIndex layer) {
        this.textureRegion = textureRegion;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.scale = scale;
        this.rotation = rotation;
        this.layer = layer;
        additionalValues = true;
    }

    public TextureDrawing(TextureRegion textureRegion, float x, float y, float w, float h, float scale, float rotation, ZIndex layer, Color color) {
        this.textureRegion = textureRegion;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.scale = scale;
        this.rotation = rotation;
        this.layer = layer;
        this.color = color;
        additionalValues = true;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (beforeDrawing != null) {
            beforeDrawing.run();
        }
        if (color != null) batch.setColor(color);

        if (additionalValues) {
            batch.draw(textureRegion,
                x, y,
                origin == null ? w / 2f : origin.x, origin == null ? h / 2f : origin.y,
                w, h,
                scale, scale,
                rotation);
        } else {
            batch.draw(textureRegion,
                x, y, w, h);
        }

        if (color != null) batch.setColor(1f, 1f, 1f, 1f);
        if (afterDrawing != null) {
            afterDrawing.run();
        }
    }

    @Override
    public ZIndex getZIndex() {
        return layer;
    }

    public TextureDrawing setBeforeDrawing(Runnable beforeDrawing) {
        this.beforeDrawing = beforeDrawing;
        return this;
    }

    public TextureDrawing setAfterDrawing(Runnable afterDrawing) {
        this.afterDrawing = afterDrawing;
        return this;
    }

    public TextureDrawing setOrigin(Vector2 origin) {
        this.origin = origin;
        return this;
    }
}
