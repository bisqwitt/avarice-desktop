package com.avaricious.screens;

import com.avaricious.Main;
import com.avaricious.components.texts.InQueueText;
import com.avaricious.effects.IdleFloatEffect;
import com.avaricious.effects.IdleSwayEffect;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.Seq;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class InQueueScreen extends ScreenAdapter {

    private final Main app;

    private final InQueueText inQueueText = new InQueueText();
    private final TextureRegion dot = Assets.I().get(AssetKey.DOT_SYMBOL);

    private final List<IdleFloatEffect> floatEffects = new ArrayList<>();
    private final List<IdleSwayEffect> swayEffects = new ArrayList<>();

    private float loadingTimer = 0f;
    private int visibleDots = 0;
    private int dotDirection = 1;
    private final float dotInterval = 0.45f;

    public InQueueScreen(Main main) {
        this.app = main;
        for (int i = 0; i < 3; i++) {
            floatEffects.add(new IdleFloatEffect());
            swayEffects.add(new IdleSwayEffect());
        }
    }

    @Override
    public void render(float delta) {
        updateLoadingDots(delta);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        SpriteBatch batch = app.getBatch();
        app.getViewport().apply();
        batch.setProjectionMatrix(app.getViewport().getCamera().combined);
        batch.begin();

        inQueueText.draw(delta);
        drawDot(0, 6.15f);
        drawDot(1, 6.7f);
        drawDot(2, 7.25f);

        Pencil.I().draw(batch, delta);

        batch.end();
    }

    private void updateLoadingDots(float delta) {
        Seq.of(floatEffects).forEach(effect -> effect.update(delta));
        Seq.of(swayEffects).forEach(effect -> effect.update(delta));
        loadingTimer += delta;

        if (loadingTimer >= dotInterval) {
            loadingTimer = 0f;

            visibleDots += dotDirection;

            if (visibleDots >= 3) {
                visibleDots = 3;
                dotDirection = -1;
            } else if (visibleDots <= 0) {
                visibleDots = 0;
                dotDirection = 1;
            }
        }
    }

    private void drawDot(int index, float x) {
        if (index >= visibleDots) return;

        Pencil.I().addDrawing(new TextureDrawing(
            dot,
            x,
            15f + floatEffects.get(index).getValue(),
            7 / 15f,
            11 / 15f,
            1f,
            swayEffects.get(index).getValue(),
            ZIndex.PACK_OPENING
        ));
    }
}
