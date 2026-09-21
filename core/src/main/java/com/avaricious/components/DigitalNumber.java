package com.avaricious.components;

import com.avaricious.effects.IdleFloatEffect;
import com.avaricious.effects.IdleScaleEffect;
import com.avaricious.effects.IdleSwayEffect;
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

public class DigitalNumber {

    private static final float DEFAULT_COMPACT_THRESHOLD = 1_000f;
    private static final float SUFFIX_GAP = 0.05f;
    private static final AssetKey[] COMPACT_SUFFIXES = {
        AssetKey.K, AssetKey.M, AssetKey.B, AssetKey.T, AssetKey.Q
    };
    private static final AssetKey[] COMPACT_SUFFIX_SHADOWS = {
        AssetKey.K_SHADOW, AssetKey.M_SHADOW, AssetKey.B_SHADOW,
        AssetKey.T_SHADOW, AssetKey.Q_SHADOW
    };

    protected final List<TextureRegion> numberTextures = new ArrayList<>();
    protected final List<TextureRegion> numberShadowTextures = new ArrayList<>();
    protected final TextureRegion dotSymbol = Assets.I().get(AssetKey.DOT_SYMBOL);
    protected final TextureRegion minusSymbol = Assets.I().get(AssetKey.MINUS_SYMBOL);

    protected Color color;
    protected final Rectangle firstDigitBounds;
    protected float offset;

    private ZIndex zIndex = ZIndex.DIGITAL_NUMBER;

    private float value;
    private float compactThreshold = DEFAULT_COMPACT_THRESHOLD;
    private boolean asDecimal = false;
    private float displayedValue;
    private int displayedDecimalPlaces;
    private TextureRegion compactSuffix;
    private TextureRegion compactSuffixShadow;

    private final PulseEffect pulseEffect = new PulseEffect();
    private final IdleFloatEffect floatEffect = new IdleFloatEffect();
    private final IdleSwayEffect swayEffect = new IdleSwayEffect(1.2f, 0.4f);
    private final IdleScaleEffect scaleEffect = new IdleScaleEffect();

    public DigitalNumber(float initialScore, Color color, Rectangle firstDigitBounds, float offset) {
        setValue(initialScore);
        this.color = color;
        this.firstDigitBounds = firstDigitBounds;
        this.offset = offset;
    }

    public DigitalNumber(float initialScore, Color color, int setLength, Rectangle firstDigitBounds, float offset) {
        value = initialScore;
        this.color = color;
        this.firstDigitBounds = firstDigitBounds;
        this.offset = offset;

        updateDisplayedValue(Math.abs(initialScore));

        for (int i = 0; i < setLength; i++) {
            numberTextures.add(Assets.I().getDigitalNumber(0));
            numberShadowTextures.add(Assets.I().getDigitalNumberShadow(0));
        }
        updateDigitalNumbers(displayedValue, displayedDecimalPlaces);
    }

    public void draw(float delta) {
        draw(delta, getScale(), getRotation());
    }

    public void draw(float delta, float scale, float rotation) {
        floatEffect.update(delta);
        swayEffect.update(delta);
        pulseEffect.update(delta);

        float numberY = calcNumberY();
        int decimalPlaces = displayedDecimalPlaces;
        int intDigitCount = numberTextures.size() - decimalPlaces;
        float x = firstDigitBounds.x;

        for (int i = 0; i < numberTextures.size(); i++) {
            TextureRegion numberTexture = numberTextures.get(i);
            float width = getGlyphWidth(numberTexture);
            float height = getGlyphHeight(numberTexture);

            Pencil.I().addDrawing(new TextureDrawing(
                numberShadowTextures.get(i),
                x, numberY - 0.1f, width, height,
                scale, rotation, getZIndex(), Assets.I().shadowColor()
            ));
            Pencil.I().addDrawing(new TextureDrawing(
                numberTexture,
                x, numberY, width, height,
                scale, rotation, getZIndex(), color
            ));

            if (i < numberTextures.size() - 1) {
                x += width + getGlyphTracking();
                if (decimalPlaces > 0 && i + 1 == intDigitCount) {
                    Pencil.I().addDrawing(new TextureDrawing(
                        dotSymbol,
                        x, numberY, getGlyphWidth(dotSymbol), getGlyphHeight(dotSymbol),
                        scale, rotation, getZIndex(), color
                    ));
                    x += getDecimalPointAdvance();
                }
            }
        }

        if (compactSuffix != null) {
            float suffixX = firstDigitBounds.x + getNumericWidth() + SUFFIX_GAP;
            float suffixWidth = getGlyphWidth(compactSuffix);
            float suffixHeight = getGlyphHeight(compactSuffix);
            Pencil.I().addDrawing(new TextureDrawing(
                compactSuffixShadow,
                suffixX, numberY - 0.1f,
                suffixWidth, suffixHeight,
                scale, rotation, getZIndex(), Assets.I().shadowColor()
            ));
            Pencil.I().addDrawing(new TextureDrawing(
                compactSuffix,
                suffixX, numberY,
                suffixWidth, suffixHeight,
                scale, rotation, getZIndex(), color
            ));
        }

        boolean isNegative = value < 0;
        if (isNegative) {
            float minusWidth = getGlyphWidth(minusSymbol);
            Pencil.I().addDrawing(new TextureDrawing(
                minusSymbol,
                firstDigitBounds.x - getGlyphTracking() - minusWidth,
                numberY,
                minusWidth,
                getGlyphHeight(minusSymbol),
                scale,
                rotation,
                getZIndex(),
                color
            ));
        }
    }

    private void updateDigitalNumbers(float score, int decimalPlaces) {
        Assets assetManager = Assets.I();

        if (decimalPlaces > 0) {
            int scaledDecimals = Math.round((score % 1) * (int) Math.pow(10, decimalPlaces));
            for (int i = numberTextures.size() - 1; i >= numberTextures.size() - decimalPlaces; i--) {
                int digit = scaledDecimals % 10;
                numberTextures.set(i, assetManager.getDigitalNumber(digit));
                numberShadowTextures.set(i, assetManager.getDigitalNumberShadow(digit));
                scaledDecimals /= 10;
            }

            long intPart = (long) score;
            for (int i = numberTextures.size() - decimalPlaces - 1; i >= 0; i--) {
                int digit = (int) (intPart % 10);
                numberTextures.set(i, assetManager.getDigitalNumber(digit));
                numberShadowTextures.set(i, assetManager.getDigitalNumberShadow(digit));
                intPart /= 10;
            }
        } else {
            long tempScore = (long) score;
            for (int i = numberTextures.size() - 1; i >= 0; i--) {
                int digit = (int) (tempScore % 10);
                numberTextures.set(i, assetManager.getDigitalNumber(digit));
                numberShadowTextures.set(i, assetManager.getDigitalNumberShadow(digit));
                tempScore /= 10;
            }
        }
    }

    public void setValue(float value) {
        this.value = value;

        float absValue = Math.abs(value);
        updateDisplayedValue(absValue);
        int intDigits = (int) displayedValue == 0
            ? 1 : (int) Math.log10(displayedValue) + 1;
        int totalDigits = intDigits + displayedDecimalPlaces;

        while (numberTextures.size() < totalDigits) {
            numberTextures.add(Assets.I().getDigitalNumber(0));
            numberShadowTextures.add(Assets.I().getDigitalNumberShadow(0));
        }
        while (numberTextures.size() > totalDigits) {
            numberTextures.remove(numberTextures.size() - 1);
            numberShadowTextures.remove(numberShadowTextures.size() - 1);
        }

        updateDigitalNumbers(displayedValue, displayedDecimalPlaces);
        pulseEffect.pulse();
    }

    private void updateDisplayedValue(float absoluteValue) {
        compactSuffix = null;
        compactSuffixShadow = null;

        if (absoluteValue < compactThreshold) {
            displayedValue = absoluteValue;
            displayedDecimalPlaces = countDecimalPlaces(absoluteValue);
            return;
        }

        float divisor = 1_000f;
        int suffixIndex = 0;
        while (suffixIndex < COMPACT_SUFFIXES.length - 1
            && absoluteValue >= divisor * 1_000f) {
            divisor *= 1_000f;
            suffixIndex++;
        }

        float compactValue = absoluteValue / divisor;
        if (compactValue < 10f) {
            compactValue = (float) Math.floor(compactValue * 100f) / 100f;
            displayedDecimalPlaces = decimalPlacesFor(compactValue, 2);
        } else if (compactValue < 100f) {
            compactValue = (float) Math.floor(compactValue * 10f) / 10f;
            displayedDecimalPlaces = decimalPlacesFor(compactValue, 1);
        } else {
            compactValue = (float) Math.floor(compactValue);
            displayedDecimalPlaces = 0;
        }

        displayedValue = compactValue;
        compactSuffix = Assets.I().get(COMPACT_SUFFIXES[suffixIndex]);
        compactSuffixShadow = Assets.I().get(COMPACT_SUFFIX_SHADOWS[suffixIndex]);
    }

    private int decimalPlacesFor(float number, int maximumPlaces) {
        if (Math.abs(number - Math.round(number)) < 0.0001f) return 0;
        if (maximumPlaces > 1
            && Math.abs(number * 10f - Math.round(number * 10f)) >= 0.001f) {
            return 2;
        }
        return 1;
    }

    private int countDecimalPlaces(float score) {
        if (!asDecimal) return 0;
        String text = Float.toString(score);
        int dotIndex = text.indexOf('.');
        if (dotIndex == -1) return 0;

        String decimals = text.substring(dotIndex + 1);
        decimals = decimals.replaceAll("0+$", "");
        return decimals.length();
    }

    public float getWidth() {
        float width = getNumericWidth();
        if (compactSuffix != null) width += SUFFIX_GAP + getGlyphWidth(compactSuffix);
        return width;
    }

    protected float getNumericWidth() {
        float width = 0f;
        int intDigitCount = numberTextures.size() - displayedDecimalPlaces;

        for (int i = 0; i < numberTextures.size(); i++) {
            width += getGlyphWidth(numberTextures.get(i));
            if (i < numberTextures.size() - 1) {
                width += getGlyphTracking();
                if (displayedDecimalPlaces > 0 && i + 1 == intDigitCount) {
                    width += getDecimalPointAdvance();
                }
            }
        }
        return width;
    }

    /** Uses the same native-texture sizing rule as FabledWord. */
    protected float getGlyphWidth(TextureRegion glyph) {
        return glyph.getRegionWidth() * getGlyphScale();
    }

    protected float getGlyphHeight(TextureRegion glyph) {
        return glyph.getRegionHeight() * getGlyphScale();
    }

    private float getGlyphScale() {
        int referenceHeight = Assets.I().getDigitalNumber(0).getRegionHeight();
        return referenceHeight == 0 ? 0f : firstDigitBounds.height / referenceHeight;
    }

    protected float getGlyphTracking() {
        return offset - getGlyphWidth(Assets.I().getDigitalNumber(0));
    }

    private float getDecimalPointAdvance() {
        return offset * 0.5f;
    }

    public float getValue() {
        return value;
    }

    public void addValue(float amount) {
        setValue(getValue() + amount);
    }

    public void subtractValue(float amount) {
        setValue(getValue() - amount);
    }

    public Rectangle getFirstDigitBounds() {
        return firstDigitBounds;
    }

    public float calcNumberY() {
        return firstDigitBounds.y + floatEffect.getValue();
    }

    public float getScale() {
        return pulseEffect.getScale() * scaleEffect.getValue();
    }

    public float getRotation() {
        return pulseEffect.getRotation() + swayEffect.getValue();
    }

    protected ZIndex getZIndex() {
        return zIndex;
    }

    public DigitalNumber setZIndex(ZIndex zIndex) {
        this.zIndex = zIndex;
        return this;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public PulseEffect getPulseEffect() {
        return pulseEffect;
    }

    public IdleFloatEffect getFloatEffect() {
        return floatEffect;
    }

    public IdleSwayEffect getSwayEffect() {
        return swayEffect;
    }

    public IdleScaleEffect getIdleScaleEffect() {
        return scaleEffect;
    }

    public DigitalNumber setAsDecimal() {
        asDecimal = true;
        setValue(value);
        return this;
    }

    public DigitalNumber setCompactThreshold(float compactThreshold) {
        if (this.compactThreshold == compactThreshold) return this;
        this.compactThreshold = compactThreshold;
        setValue(value);
        return this;
    }

    public DigitalNumber setDigitSpacing(float offset) {
        this.offset = offset;
        return this;
    }
}
