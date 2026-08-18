package com.avaricious.components.popups;

import com.avaricious.effects.PulseEffect;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class NumberPopup implements IPopup {

    public final static float defaultWidth = 7 / 16f;
    public final static float defaultHeight = 11 / 16f;

    private enum Phase {PULSE, HOLD, EXIT, FINISHED}

    protected final List<TextureRegion> digitalNumberTextures = new ArrayList<>();
    protected final List<TextureRegion> digitalNumberShadowTextures = new ArrayList<>();

    private final TextureRegion plusTexture = Assets.I().get(AssetKey.PLUS_SYMBOL);
    private final TextureRegion minusTexture = Assets.I().get(AssetKey.MINUS_SYMBOL);
    private final TextureRegion percentageTexture = Assets.I().get(AssetKey.PERCENTAGE_SYMBOL);

    private final int number;

    protected final Rectangle bounds;
    private final Color color;

    protected final float numberOffset;
    protected final float pulseTime = 0.20f; // pop+wobble duration
    protected final float holdTime = 0.40f; // OLD behavior: stay static for this long
    protected final float exitTime = 0.25f; // shrink until gone

    private final boolean manualHold;

    private Phase phase = Phase.PULSE;

    // One timer reused for the current phase (0..phaseDuration)
    private float timeInPhase = 0f;

    private final boolean asPercentage;

    private Runnable onFinished;

    protected PulseEffect pulseEffect = new PulseEffect();
    protected ZIndex zIndex = ZIndex.POPUP_DEFAULT;

    public NumberPopup(int number, Color color, float x, float y, boolean asPercentage, boolean manualHold) {
        this(number, color, new Rectangle(x, y, defaultWidth, defaultHeight), asPercentage, manualHold);
    }

    public NumberPopup(int number, Color color, Rectangle bounds, boolean asPercentage, boolean manualHold) {
        this.number = number;
        this.color = color;
        this.asPercentage = asPercentage;
        this.manualHold = manualHold;

        this.bounds = new Rectangle(bounds);
        setDigitalNumberTextures(number);
        restart();

        float defaultOffset = 0.5f;
        numberOffset = bounds.width == defaultWidth && bounds.height == defaultHeight
            ? defaultOffset
            : defaultOffset + (bounds.width - defaultWidth) * 0.5f;

        pulseEffect.pulse();
    }

    public void release() {
        if (manualHold && phase == Phase.HOLD) {
            phase = Phase.EXIT;
            timeInPhase = 0f;
        }
    }

    public boolean isManualHold() {
        return manualHold;
    }

    public void transform(int newValue) {
        setDigitalNumberTextures(newValue);
        restart();
    }

    private void restart() {
        phase = Phase.PULSE;
        timeInPhase = 0f;
    }

    @Override
    public boolean isFinished() {
        return phase == Phase.FINISHED;
    }

    @Override
    public void update(float delta) {
        if (phase == Phase.FINISHED) return;
        pulseEffect.update(delta);

        timeInPhase += delta;

        switch (phase) {
            case PULSE:
                if (timeInPhase >= pulseTime) {
                    phase = Phase.HOLD;
                    timeInPhase = 0f;
                }
                break;

            case HOLD:
                // Old behavior: auto-exit after holdTime
                if (!manualHold && timeInPhase >= holdTime) {
                    phase = Phase.EXIT;
                    timeInPhase = 0f;
                }
                break;
            case EXIT:
                if (timeInPhase >= exitTime) {
                    timeInPhase = exitTime;
                    phase = Phase.FINISHED;
                    if (onFinished != null) onFinished.run();
                }
                break;
            case FINISHED:
                break;
        }
    }

    @Override
    public void draw(float delta) {
        if (phase == Phase.FINISHED) return;

        float scale = pulseEffect.getScale();
        float rotation = pulseEffect.getRotation();
        float alpha = getAlpha();

        if (alpha <= 0f || scale <= 0f) return;

        float yOffset = getPulseYOffset();

        // Use alpha for main draw color
        Pencil.I().addDrawing(new TextureDrawing(
            number < 0 ? minusTexture : plusTexture,
            bounds.x - numberOffset, bounds.y + yOffset, bounds.width, bounds.height,
            scale, rotation, zIndex, new Color(color.r, color.g, color.b, alpha)
        ));

        for (int i = 0; i < digitalNumberTextures.size(); i++) {
            int index = i;
            Pencil.I().addDrawing(new TextureDrawing(
                digitalNumberTextures.get(index),
                bounds.x + (numberOffset * index), bounds.y + yOffset, bounds.width, bounds.height,
                scale, rotation, zIndex, new Color(color.r, color.g, color.b, alpha)
            ));
        }

        if (asPercentage) {
            Pencil.I().addDrawing(new TextureDrawing(
                percentageTexture,
                bounds.x + 0.4f, bounds.y + yOffset, 8 / 20f, 13 / 20f,
                scale, rotation, zIndex
            ));
        }
    }

    protected float getPulseYOffset() {
        if (phase != Phase.PULSE) return 0f;
        float t = clamp01(timeInPhase / pulseTime);

        // Up-kick early, settle back
        float kick = (float) Math.sin(Math.PI * t) * 0.06f; // tune (world units)
        return kick;
    }

    protected float getAlpha() {
        if (phase == Phase.PULSE || phase == Phase.HOLD) {
            return 1f;
        }

        // EXIT fade
        float t = clamp01(timeInPhase / exitTime);
        return 1f - t;
    }

    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private void setDigitalNumberTextures(int number) {
        digitalNumberTextures.clear();
        digitalNumberShadowTextures.clear(); // IMPORTANT: keep lists in sync

        for (char c : String.valueOf(number).toCharArray()) {
            int digit = c - '0';
            digitalNumberTextures.add(Assets.I().getDigitalNumber(digit));
//            digitalNumberShadowTextures.add(new TextureRegion(Assets.I().getDigitalNumberShadow(digit)));
        }
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public NumberPopup setZIndex(ZIndex zIndex) {
        this.zIndex = zIndex;
        return this;
    }
}
