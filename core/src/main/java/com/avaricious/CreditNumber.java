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

    public CreditNumber(int initialScore, Rectangle rectangle, float offset) {
        super(initialScore, Assets.I().yellow(), rectangle, offset);
    }

    @Override
    public void draw(float delta) {
        super.draw(delta);
        draw(delta, getScale(), getRotation());
    }

    @Override
    public void draw(float delta, float scale, float rotation) {
        super.draw(delta, scale, rotation);
        float x = firstDigitBounds.x + (numberTextures.size() * offset) + 0.05f;
        float y = calcNumberY();

        Pencil.I().addDrawing(new TextureDrawing(
            dollarSymbolShadow,
            x, y - 0.1f, firstDigitBounds.width, firstDigitBounds.height,
            scale, rotation, getZIndex(), new Color(color.r, color.g, color.b, Assets.I().shadowColor().a)
        ));

        Pencil.I().addDrawing(new TextureDrawing(
            dollarSymbol,
            x, y, firstDigitBounds.width, firstDigitBounds.height,
            scale, rotation,
            getZIndex(), color));
    }

    @Override
    public float getWidth() {
        return super.getWidth() + 0.05f + firstDigitBounds.width;
    }

    @Override
    public CreditNumber setZIndex(ZIndex zIndex) {
        return (CreditNumber) super.setZIndex(zIndex);
    }
}
