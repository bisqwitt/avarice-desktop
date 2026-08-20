package com.avaricious.components;

import com.avaricious.screens.ScreenManager;
import com.avaricious.screens.SlotScreen;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/** Casino progression meter filled by claiming bouncing rewards. */
public class CompChipBar {

    private static CompChipBar instance;

    public static CompChipBar I() {
        return instance == null ? instance = new CompChipBar() : instance;
    }

    private static final float X = 0f;
    private static final float Y = 8.82f;
    private static final float WIDTH = 16f;
    private static final float HEIGHT = 0.18f;

    private static final Color BACKGROUND_COLOR =
        new Color(0.12f, 0.12f, 0.15f, 1f);
    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final TextureRegion spade = Assets.I().get(AssetKey.SPADE);

    private int level = 1;
    private int chips = 0;
    private int chipsRequiredFirstLevel = 15;
    private int chipsRequired = chipsRequiredFirstLevel;

    private float displayedProgress = 0f;
    private float gainPulse = 0f;
    private float levelUpPulse = 0f;
    private float shine = 0f;
    private float time = 0f;

    private CompChipBar() {
    }

    public void draw(float delta) {
        time += delta;

        float progress = Math.min(1f, (float) chips / chipsRequired);
        displayedProgress = MathUtils.lerp(
            displayedProgress,
            progress,
            Math.min(1f, delta * 12f)
        );

        gainPulse = Math.max(0f, gainPulse - delta * 3.8f);
        levelUpPulse = Math.max(0f, levelUpPulse - delta * 1.8f);
        shine = Math.max(0f, shine - delta * 2.5f);

        float pulseWave = MathUtils.sin((1f - gainPulse) * MathUtils.PI);
        float gainGlow = gainPulse > 0f ? pulseWave * pulseWave : 0f;
        float renderHeight = HEIGHT * (
            1f + pulseWave * 0.65f + levelUpPulse * 0.55f
        );
        float renderY = Y - (renderHeight - HEIGHT) / 2f;

        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            X,
            renderY,
            WIDTH,
            renderHeight,
            ZIndex.SHOP,
            BACKGROUND_COLOR
        ));

        float rainbow = (MathUtils.sin(time * 3.5f) + 1f) * 0.5f;
        float baseRed = MathUtils.lerp(0.52f, 0.84f, rainbow);
        float baseGreen = MathUtils.lerp(0.28f, 0.48f, 1f - rainbow);
        float glowMix = gainGlow * 0.82f;
        Color chipBarColor = new Color(
            MathUtils.lerp(baseRed, 1f, glowMix),
            MathUtils.lerp(baseGreen, 1f, glowMix),
            1f,
            1f
        );

        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            X,
            renderY,
            WIDTH * displayedProgress,
            renderHeight,
            ZIndex.SHOP,
            chipBarColor
        ));

        if (progress > displayedProgress) {
            Pencil.I().addDrawing(new TextureDrawing(
                whitePixel,
                X + WIDTH * displayedProgress,
                renderY,
                WIDTH * (progress - displayedProgress),
                renderHeight,
                ZIndex.SHOP,
                new Color(0.95f, 0.82f, 1f, 0.72f)
            ));
        }

        float leadingX = X + WIDTH * displayedProgress;
        float capWidth = 0.035f + shine * 0.09f;
        if (displayedProgress > 0.002f) {
            Pencil.I().addDrawing(new TextureDrawing(
                whitePixel,
                leadingX - capWidth / 2f,
                renderY - shine * 0.035f,
                capWidth,
                renderHeight + shine * 0.07f,
                ZIndex.SHOP,
                new Color(1f, 1f, 1f, 0.55f + shine * 0.45f)
            ));
        }

        drawSuitMarker(leadingX, pulseWave);

        if (levelUpPulse > 0f) {
            Pencil.I().addDrawing(new TextureDrawing(
                whitePixel,
                X,
                renderY,
                WIDTH,
                renderHeight,
                ZIndex.SHOP,
                new Color(1f, 1f, 1f, levelUpPulse * levelUpPulse)
            ));
        }
    }

    private void drawSuitMarker(float leadingX, float pulseWave) {
        float size = 0.29f;
        float markerX = MathUtils.clamp(
            leadingX - size / 2f,
            0.03f,
            WIDTH - size - 0.03f
        );
        float markerY = 8.67f + pulseWave * 0.025f;
        float scale = 1f + pulseWave * 0.22f + levelUpPulse * 0.18f;

        Pencil.I().addDrawing(new TextureDrawing(
            spade,
            markerX,
            markerY,
            size,
            size,
            scale,
            0f,
            ZIndex.SHOP
        ));
    }

    public void addChips(int amount) {
        chips += amount;
        gainPulse = 1f;
        shine = 1f;

        while (chips >= chipsRequired) {
            chips -= chipsRequired;
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        displayedProgress = 0f;
        levelUpPulse = 1f;
        shine = 1f;
        chipsRequired = calculateChipsRequired(level);

        ScreenManager.I().getScreen(SlotScreen.class).getLevelUpWindow().show();
    }

    private int calculateChipsRequired(int level) {
        return chipsRequiredFirstLevel + (level - 1) * 5;
    }

    public int getLevel() {
        return level;
    }

    public int getChips() {
        return chips;
    }

    public int getChipsRequired() {
        return chipsRequired;
    }

    public float getProgress() {
        return (float) chips / chipsRequired;
    }
}
