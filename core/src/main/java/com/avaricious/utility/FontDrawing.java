package com.avaricious.utility;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class FontDrawing implements Drawing {

    private final ZIndex layer;

    private final BitmapFont font;
    private final Vector2 pos;

    private final GlyphLayout text;

    private final float scale;
    private final float rotation;

    public FontDrawing(BitmapFont font, GlyphLayout txt, Vector2 pos, ZIndex layer) {
        this.font = font;
        this.text = txt;
        this.pos = pos;
        this.layer = layer;

        scale = 1;
        rotation = 0;
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.setProjectionMatrix(GameContext.I().uiViewport.getCamera().combined);
        font.draw(batch, text, pos.x, pos.y);
        batch.setProjectionMatrix(GameContext.I().viewport.getCamera().combined);
    }

    @Override
    public ZIndex getZIndex() {
        return layer;
    }
}
