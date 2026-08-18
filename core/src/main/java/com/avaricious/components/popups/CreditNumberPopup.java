package com.avaricious.components.popups;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class CreditNumberPopup extends NumberPopup {

    private final TextureRegion dollarSymbol = Assets.I().get(AssetKey.DOLLAR_SYMBOL);

    public CreditNumberPopup(int number, float x, float y, boolean asPercentage, boolean manualHold) {
        super(number, Assets.I().yellow(), x, y, asPercentage, manualHold);
    }

    public CreditNumberPopup(int number, Rectangle bounds, boolean asPercentage, boolean manualHold) {
        super(number, Assets.I().yellow(), bounds, asPercentage, manualHold);
    }

    @Override
    public void draw(float delta) {
        super.draw(delta);

        float scale = pulseEffect.getScale();
        float rotation = pulseEffect.getRotation();
        float alpha = getAlpha();

        Color color = Assets.I().yellow();
        Pencil.I().addDrawing(new TextureDrawing(
            dollarSymbol,
            bounds.x + (numberOffset * digitalNumberTextures.size()), bounds.y, bounds.width, bounds.height,
            scale, rotation, zIndex, new Color(color.r, color.g, color.b, alpha)
        ));
    }
}
