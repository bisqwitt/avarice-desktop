package com.avaricious.components.popups;

import com.avaricious.effects.PulseEffect;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.EconomyScaling;
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

    private float number;

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

    /* Total age lets specialized reward popups define unique motion. */
    protected float animationAge = 0f;

    private final boolean asPercentage;

    private Runnable onFinished;

    protected PulseEffect pulseEffect = new PulseEffect();
    protected ZIndex zIndex = ZIndex.POPUP_DEFAULT;

    public NumberPopup(float number, Color color, float x, float y, boolean asPercentage, boolean manualHold) {
        this(number, color, new Rectangle(x, y, defaultWidth, defaultHeight), asPercentage, manualHold);
    }

    public NumberPopup(float number, Color color, Rectangle bounds, boolean asPercentage, boolean manualHold) {
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

        pulseEffect.setStrength(2f);
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

    public void transform(float newValue) {
        number = newValue;
        setDigitalNumberTextures(newValue);
        restart();
    }

    private void restart() {
        phase = Phase.PULSE;
        timeInPhase = 0f;
        animationAge = 0f;
    }

    @Override
    public boolean isFinished() {
        return phase == Phase.FINISHED;
    }

    @Override
    public void update(float delta) {
        if (phase == Phase.FINISHED) return;
        animationAge += delta;
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

        float xOffset = getRenderXOffset();
        float yOffset = getPulseYOffset();
        TextureRegion sign = number < 0 ? minusTexture : plusTexture;
        float signWidth = getGlyphWidth(sign);

        // Use alpha for main draw color
        Pencil.I().addDrawing(new TextureDrawing(
            sign,
            bounds.x - getGlyphTracking() - signWidth + xOffset,
            bounds.y + yOffset, signWidth, getGlyphHeight(sign),
            scale, rotation, zIndex, new Color(color.r, color.g, color.b, alpha)
        ));

        float x = bounds.x;
        for (TextureRegion numberTexture : digitalNumberTextures) {
            Pencil.I().addDrawing(new TextureDrawing(
                numberTexture,
                x + xOffset, bounds.y + yOffset,
                getGlyphWidth(numberTexture), getGlyphHeight(numberTexture),
                scale, rotation, zIndex, new Color(color.r, color.g, color.b, alpha)
            ));
            x += getGlyphWidth(numberTexture) + getGlyphTracking();
        }

        if (asPercentage) {
            Pencil.I().addDrawing(new TextureDrawing(
                percentageTexture,
                x + xOffset, bounds.y + yOffset,
                getGlyphWidth(percentageTexture), getGlyphHeight(percentageTexture),
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

    protected float getRenderXOffset() {
        return 0f;
    }

    protected float getTrailingSymbolX() {
        float x = bounds.x;
        for (TextureRegion numberTexture : digitalNumberTextures) {
            x += getGlyphWidth(numberTexture) + getGlyphTracking();
        }
        return x;
    }

    protected float getGlyphWidth(TextureRegion glyph) {
        return glyph.getRegionWidth() * getGlyphScale();
    }

    protected float getGlyphHeight(TextureRegion glyph) {
        return glyph.getRegionHeight() * getGlyphScale();
    }

    private float getGlyphScale() {
        int referenceHeight = Assets.I().getDigitalNumber(0).getRegionHeight();
        return referenceHeight == 0 ? 0f : bounds.height / referenceHeight;
    }

    private float getGlyphTracking() {
        return numberOffset - getGlyphWidth(Assets.I().getDigitalNumber(0));
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

    private void setDigitalNumberTextures(float number) {
        digitalNumberTextures.clear();
        digitalNumberShadowTextures.clear(); // IMPORTANT: keep lists in sync

        String displayValue = asPercentage
            ? Long.toString((long) Math.floor(Math.abs(number)))
            : EconomyScaling.compact(number);
        for (char character : displayValue.toCharArray()) {
            if (Character.isDigit(character)) {
                int digit = Character.getNumericValue(character);
                digitalNumberTextures.add(Assets.I().getDigitalNumber(digit));
            } else {
                digitalNumberTextures.add(Assets.I().get(numberAsset(character)));
            }
//            digitalNumberShadowTextures.add(new TextureRegion(Assets.I().getDigitalNumberShadow(digit)));
        }
    }

    private AssetKey numberAsset(char character) {
        switch (character) {
            case '.': return AssetKey.DOT_SYMBOL;
            case 'k': return AssetKey.K;
            case 'm': return AssetKey.M;
            case 'b': return AssetKey.B;
            case 't': return AssetKey.T;
            case 'q': return AssetKey.Q;
            default: throw new IllegalArgumentException("Unsupported compact number symbol");
        }
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public NumberPopup setZIndex(ZIndex zIndex) {
        this.zIndex = zIndex;
        return this;
    }

    public Color getColor() {
        return color;
    }
}
