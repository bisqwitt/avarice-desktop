package com.avaricious.utility;

import com.avaricious.DevTools;
import com.avaricious.effects.BorderPulseMesh;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Pencil {

    private static Pencil instance;

    public static Pencil I() {
        return instance == null ? instance = new Pencil() : instance;
    }

    private Pencil() {
    }

    private SpriteBatch batch;

    private final List<Drawing> drawings = new ArrayList<>();

    private final TextureRegion feltPixel = Assets.I().get(AssetKey.FELT_PIXEL);
    private final TextureRegion charcoal = Assets.I().get(AssetKey.CHARCOAL_PIXEL);

    private Rectangle scissors;
    private Runnable beforeDrawing;

    private boolean dimBackground = false;
    private ZIndex dimLayer = null;
    private float dimAlpha = 0f;
    private float targetDimAlpha = 0f;
    private final float dimSpeed = 15f; // higher = faster fade

    private final GlyphLayout mouseLocationTxt = new GlyphLayout();

    public void draw(SpriteBatch batch, float delta) {

        updateDarkenAnimation(Gdx.graphics.getDeltaTime());
        BorderPulseMesh.I().render(batch, Gdx.graphics.getDeltaTime());

        Collections.sort(drawings, new Comparator<Drawing>() {
            @Override
            public int compare(Drawing a, Drawing b) {
                return Integer.compare(a.getZIndex().index(), b.getZIndex().index());
            }
        });
        for (Drawing drawing : drawings) {
            drawing.draw(batch);
        }
        drawings.clear();

        if (DevTools.showMouseLocation()) {
            Vector2 mouseLocation = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            GameContext.I().viewport.unproject(mouseLocation);
            mouseLocationTxt.setText(Assets.I().getSmallFont(), mouseLocation.x + "/" + mouseLocation.y);
            new FontDrawing(Assets.I().getSmallFont(), mouseLocationTxt, new Vector2(mouseLocation.x * 100, mouseLocation.y * 100), ZIndex.PACK_OPENING)
                .draw(batch);
        }
    }

    private void updateDarkenAnimation(float delta) {
        targetDimAlpha = dimBackground ? 0.25f : 0f;

        // smooth movement toward target
        dimAlpha = Interpolation.fade.apply(
            dimAlpha,
            targetDimAlpha,
            Math.min(1f, delta * dimSpeed)
        );
    }

    public void addDrawing(Drawing drawing) {
        if (beforeDrawing != null && drawing instanceof TextureDrawing) {
            ((TextureDrawing) drawing).setBeforeDrawing(beforeDrawing);
            beforeDrawing = null;
        }
        drawings.add(drawing);
    }

    public void toggleDarkenEverythingBehindLayer(ZIndex layer) {
        dimBackground = !dimBackground;
        dimLayer = layer;
    }

    public void drawDarkenWindow() {
        if (dimAlpha <= 0.01f) return;

        Pencil.I().addDrawing(new TextureDrawing(
            charcoal,
            -1f, -1f,
            GameContext.I().viewport.getWorldWidth() + 2,
            GameContext.I().viewport.getWorldHeight() + 2
            , dimLayer, Assets.I().shadowColor()
        ));
    }

    public void startScissors(Camera cam, Matrix4 matrix, Rectangle area) {
        scissors = new Rectangle();
        beforeDrawing = () -> {
            ScissorStack.calculateScissors(cam, matrix, area, scissors);
            batch.flush();
            ScissorStack.pushScissors(scissors);
        };
    }

    public void endScissors() {
        Drawing drawing = drawings.get(drawings.size() - 1);
        if (drawing instanceof TextureDrawing)
            ((TextureDrawing) drawing).setAfterDrawing(() -> {
                batch.flush();
                ScissorStack.popScissors();
            });
    }

    public void setBatch(SpriteBatch batch) {
        this.batch = batch;
    }
}
