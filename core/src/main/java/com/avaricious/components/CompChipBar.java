package com.avaricious.components;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.avaricious.game.run.ChipProgress;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/** Casino progression meter filled by claiming bouncing rewards. */
public class CompChipBar {

    private static CompChipBar instance;
    private static ChipProgress configuredProgress;

    public static void configure(ChipProgress progress) {
        if (instance != null) {
            throw new IllegalStateException("CompChipBar is already initialized");
        }
        configuredProgress = progress;
    }

    public static CompChipBar I() {
        if (configuredProgress == null) {
            throw new IllegalStateException(
                "CompChipBar must be configured with run progression"
            );
        }
        return instance == null
            ? instance = new CompChipBar(configuredProgress)
            : instance;
    }

    private static final float X = 0f;
    private static final float Y = 8.82f;
    private static final float WIDTH = 16f;
    private static final float HEIGHT = 0.18f;
    private static final Color BACKGROUND_COLOR =
        new Color(0.12f, 0.12f, 0.15f, 1f);
    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final TextureRegion spade = Assets.I().get(AssetKey.SPADE);

    private final ChipProgress progress;

    private float displayedProgress = 0f;
    private float gainPulse = 0f;
    private float levelUpPulse = 0f;
    private float shine = 0f;
    private float time = 0f;

    private CompChipBar(ChipProgress progress) {
        this.progress = progress;
    }

    public void draw(float delta) {
        time += delta;

        float progressValue = Math.min(1f, progress.getProgress());
        displayedProgress = MathUtils.lerp(
            displayedProgress,
            progressValue,
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

        if (progressValue > displayedProgress) {
            Pencil.I().addDrawing(new TextureDrawing(
                whitePixel,
                X + WIDTH * displayedProgress,
                renderY,
                WIDTH * (progressValue - displayedProgress),
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
        int levelsGained = progress.add(amount);
        gainPulse = 1f;
        shine = 1f;

        if (levelsGained > 0) {
            displayedProgress = 0f;
            levelUpPulse = 1f;
        }
    }

    public int getLevel() {
        return progress.getLevel();
    }

    public int getChips() {
        return progress.getChips();
    }

    public int getChipsRequired() {
        return progress.getChipsRequired();
    }

    public void restore(int savedLevel, int savedChips) {
        progress.restore(savedLevel, savedChips);
        displayedProgress = getProgress();
        gainPulse = 0f;
        levelUpPulse = 0f;
        shine = 0f;
    }

    public float getProgress() {
        return progress.getProgress();
    }
}
