package com.avaricious;

import com.avaricious.components.DigitalNumber;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class CreditNumber extends DigitalNumber {

    private final TextureRegion dollarSymbol = Assets.I().get(AssetKey.DOLLAR_SYMBOL);
    private final TextureRegion dollarSymbolShadow = Assets.I().get(AssetKey.DOLLAR_SYMBOL_SHADOW);
    private final TextureRegion plusSymbol = Assets.I().get(AssetKey.PLUS_SYMBOL);
    private final TextureRegion plusSymbolShadow = Assets.I().get(AssetKey.PLUS_SYMBOL_SHADOW);
    private boolean showPositiveSign;

    public CreditNumber(float initialScore, Rectangle rectangle, float offset) {
        super(initialScore, Assets.I().yellow(), rectangle, offset);
    }

    @Override
    public void draw(float delta) {
        draw(delta, getScale(), getRotation());
    }

    @Override
    public void draw(float delta, float scale, float rotation) {
        float originalX = firstDigitBounds.x;
        float signAdvance = getLeadingSignAdvance();
        firstDigitBounds.x += signAdvance;

        try {
            super.draw(delta, scale, rotation);
            float x = firstDigitBounds.x + super.getWidth() + currencyGap();
            float y = calcNumberY();
            float width = getGlyphWidth(dollarSymbol);
            float height = getGlyphHeight(dollarSymbol);

            Pencil.I().addDrawing(new TextureDrawing(
                dollarSymbolShadow,
                x, y - 0.1f, width, height,
                scale, rotation, getZIndex(), new Color(color.r, color.g, color.b, Assets.I().shadowColor().a)
            ));

            Pencil.I().addDrawing(new TextureDrawing(
                dollarSymbol,
                x, y, width, height,
                scale, rotation,
                getZIndex(), color));

            if (showPositiveSign && getValue() >= 0f) {
                float plusWidth = getGlyphWidth(plusSymbol);
                float plusHeight = getGlyphHeight(plusSymbol);
                float plusX = firstDigitBounds.x - getGlyphTracking() - plusWidth;

                Pencil.I().addDrawing(new TextureDrawing(
                    plusSymbolShadow,
                    plusX, y - 0.1f, plusWidth, plusHeight,
                    scale, rotation, getZIndex(),
                    new Color(color.r, color.g, color.b, Assets.I().shadowColor().a)
                ));
                Pencil.I().addDrawing(new TextureDrawing(
                    plusSymbol,
                    plusX, y, plusWidth, plusHeight,
                    scale, rotation, getZIndex(), color
                ));
            }
        } finally {
            firstDigitBounds.x = originalX;
        }
    }

    @Override
    public float getWidth() {
        return getLeadingSignAdvance()
            + super.getWidth()
            + currencyGap()
            + getGlyphWidth(dollarSymbol);
    }

    private float getLeadingSignAdvance() {
        if (getValue() < 0f) {
            return getGlyphWidth(minusSymbol) + getGlyphTracking();
        }
        if (showPositiveSign) {
            return getGlyphWidth(plusSymbol) + getGlyphTracking();
        }
        return 0f;
    }

    private float currencyGap() {
        return Math.max(0.05f, getGlyphTracking());
    }

    @Override
    public CreditNumber setZIndex(ZIndex zIndex) {
        return (CreditNumber) super.setZIndex(zIndex);
    }

    public CreditNumber setShowPositiveSign(boolean showPositiveSign) {
        this.showPositiveSign = showPositiveSign;
        return this;
    }
}
