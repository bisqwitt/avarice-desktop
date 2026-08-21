package com.avaricious.components.popups;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/** A missed bouncing reward fizzling out before it can reach the XP bar. */
public class LostSymbolPopup implements IPopup {

    private static final float FREEZE_DURATION = 0.10f;
    private static final float LIFETIME = 0.68f;
    private static final float SPADE_DELAY = 0.13f;
    private static final float XP_BAR_Y = 8.81f;
    private static final int FRAGMENT_COUNT = 12;

    private final TextureRegion symbol;
    private final TextureRegion whiteSymbol;
    private final TextureRegion shadowSymbol;
    private final TextureRegion spade = Assets.I().get(AssetKey.SPADE);
    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);

    private final float centerX;
    private final float centerY;
    private final float width;
    private final float height;
    private final float startRotation;
    private final float rotationDirection;

    private final float[] fragmentVelocityX = new float[FRAGMENT_COUNT];
    private final float[] fragmentVelocityY = new float[FRAGMENT_COUNT];
    private final float[] fragmentSize = new float[FRAGMENT_COUNT];
    private final float[] fragmentRotation = new float[FRAGMENT_COUNT];

    private float age = 0f;

    public LostSymbolPopup(
        Symbol lostSymbol,
        float centerX,
        float centerY,
        float width,
        float height,
        float rotation
    ) {
        symbol = Assets.I().getSymbol(lostSymbol);
        whiteSymbol = Assets.I().get(lostSymbol.whiteKey());
        shadowSymbol = Assets.I().get(lostSymbol.shadowKey());
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.startRotation = rotation;
        rotationDirection = MathUtils.randomBoolean() ? 1f : -1f;

        for (int i = 0; i < FRAGMENT_COUNT; i++) {
            fragmentVelocityX[i] = MathUtils.random(-1.20f, 1.20f);
            fragmentVelocityY[i] = MathUtils.random(0.35f, 1.30f);
            fragmentSize[i] = MathUtils.random(0.04f, 0.095f);
            fragmentRotation[i] = MathUtils.random(90f, 260f);
        }
    }

    @Override
    public void update(float delta) {
        age += delta;
    }

    @Override
    public void draw(float delta) {
        if (isFinished()) return;

        drawLostSymbol();
        drawFragments();
        drawFailedSpade();
    }

    private void drawLostSymbol() {
        float progress = fadeProgress();
        float smooth = smoothStep(progress);
        float fall = smooth * 0.82f;
        float scale = MathUtils.lerp(1.12f, 0.12f, smooth);
        float rotation = startRotation + rotationDirection * smooth * 36f;
        float drawX = centerX - width / 2f;
        float drawY = centerY - fall - height / 2f;
        float alpha = 1f - smooth * 0.88f;
        float flash = 1f - MathUtils.clamp(age / FREEZE_DURATION, 0f, 1f);
        float warning = Math.max(0f, MathUtils.sin(
            MathUtils.clamp(age / 0.22f, 0f, 1f) * MathUtils.PI
        ));

        Pencil.I().addDrawing(new TextureDrawing(
            whiteSymbol,
            drawX,
            drawY,
            width,
            height,
            scale * (1.35f + warning * 0.55f),
            rotation,
            ZIndex.POPUP_DEFAULT,
            new Color(1f, 0.04f, 0.08f, warning * 0.34f)
        ));

        Pencil.I().addDrawing(new TextureDrawing(
            shadowSymbol,
            drawX,
            drawY - 0.07f,
            width,
            height,
            scale,
            rotation,
            ZIndex.POPUP_DEFAULT,
            new Color(0.18f, 0.01f, 0.025f, alpha * 0.78f)
        ));

        Pencil.I().addDrawing(new TextureDrawing(
            symbol,
            drawX,
            drawY,
            width,
            height,
            scale,
            rotation,
            ZIndex.POPUP_DEFAULT,
            new Color(1f, 1f, 1f, alpha * (1f - smooth * 0.55f))
        ));

        Pencil.I().addDrawing(new TextureDrawing(
            whiteSymbol,
            drawX,
            drawY,
            width,
            height,
            scale * (1f + flash * 0.14f),
            rotation,
            ZIndex.POPUP_DEFAULT,
            new Color(0.98f, 0.12f, 0.16f, alpha * (0.52f + flash * 0.48f))
        ));
    }

    private void drawFragments() {
        if (age < FREEZE_DURATION) return;

        float fragmentAge = age - FREEZE_DURATION;
        float alpha = 1f - fadeProgress();

        for (int i = 0; i < FRAGMENT_COUNT; i++) {
            float size = fragmentSize[i];
            float x = centerX + fragmentVelocityX[i] * fragmentAge;
            float y = centerY
                + fragmentVelocityY[i] * fragmentAge
                - 3.8f * fragmentAge * fragmentAge;

            Pencil.I().addDrawing(new TextureDrawing(
                whitePixel,
                x - size / 2f,
                y - size / 2f,
                size,
                size,
                1f,
                fragmentRotation[i] * fragmentAge,
                ZIndex.POPUP_DEFAULT,
                new Color(0.86f, 0.025f, 0.065f, alpha * alpha * 0.95f)
            ));
        }
    }

    private void drawFailedSpade() {
        float progress = MathUtils.clamp(
            (age - SPADE_DELAY) / (LIFETIME - SPADE_DELAY),
            0f,
            1f
        );
        if (progress <= 0f) return;

        float visibility = (float) Math.pow(
            Math.max(0f, MathUtils.sin(progress * MathUtils.PI)),
            0.65f
        );
        float size = MathUtils.lerp(0.48f, 0.27f, progress);
        float x = centerX + MathUtils.sin(progress * MathUtils.PI) * 0.12f;
        float rise = MathUtils.clamp(
            (XP_BAR_Y - centerY) * 0.45f,
            0f,
            0.64f
        );
        float y = centerY + smoothStep(progress) * rise;

        Pencil.I().addDrawing(new TextureDrawing(
            spade,
            x - size / 2f,
            y - size / 2f - 0.035f,
            size,
            size,
            1.22f,
            rotationDirection * progress * 12f,
            ZIndex.POPUP_DEFAULT,
            new Color(0f, 0f, 0f, visibility * 0.58f)
        ));

        Pencil.I().addDrawing(new TextureDrawing(
            spade,
            x - size / 2f,
            y - size / 2f,
            size,
            size,
            1f,
            rotationDirection * progress * 12f,
            ZIndex.POPUP_DEFAULT,
            new Color(0.96f, 0.08f, 0.14f, visibility * 0.86f)
        ));
    }

    private float fadeProgress() {
        return MathUtils.clamp(
            (age - FREEZE_DURATION) / (LIFETIME - FREEZE_DURATION),
            0f,
            1f
        );
    }

    private float smoothStep(float value) {
        return value * value * (3f - 2f * value);
    }

    @Override
    public boolean isFinished() {
        return age >= LIFETIME;
    }
}
