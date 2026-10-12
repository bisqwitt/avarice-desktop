package com.avaricious;

import com.avaricious.components.DigitalNumber;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** A digital number followed by the icon used for spendable skill-tree spades. */
public final class SpadeNumber extends DigitalNumber {

    private final TextureRegion spade = Assets.I().get(AssetKey.SPADE);

    public SpadeNumber(float value, Rectangle rectangle, float offset) {
        super(value, Assets.I().yellow(), rectangle, offset);
    }

    @Override
    public void draw(float delta, float scale, float rotation) {
        super.draw(delta, scale, rotation);

        float x = firstDigitBounds.x + super.getWidth() + currencyGap();
        float y = calcNumberY();
        float width = getGlyphWidth(spade);
        float height = getGlyphHeight(spade);

        Pencil.I().addDrawing(new TextureDrawing(
            spade,
            x, y - 0.1f, width, height,
            scale, rotation, getZIndex(), Assets.I().shadowColor()
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            spade,
            x, y, width, height,
            scale, rotation, getZIndex(), color
        ));
    }

    @Override
    public float getWidth() {
        return super.getWidth() + currencyGap() + getGlyphWidth(spade);
    }

    private float currencyGap() {
        return Math.max(0.05f, getGlyphTracking());
    }

    @Override
    public SpadeNumber setZIndex(ZIndex zIndex) {
        return (SpadeNumber) super.setZIndex(zIndex);
    }
}
