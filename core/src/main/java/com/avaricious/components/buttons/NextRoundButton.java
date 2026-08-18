package com.avaricious.components.buttons;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class NextRoundButton extends Button {

    private final float txtWidth = 76 / 60f;
    private final float txtHeight = 52f / 60;

    private final TextureRegion nextRoundTxt = Assets.I().get(AssetKey.NEXT_ROUND_TXT);

    public NextRoundButton(Runnable onButtonPressedRunnable, Rectangle buttonRectangle, int key, ZIndex layer) {
        super(onButtonPressedRunnable, Assets.I().get(AssetKey.NEXT_ROUND_BUTTON), Assets.I().get(AssetKey.NEXT_ROUND_BUTTON_PRESSED), Assets.I().get(AssetKey.NEXT_ROUND_BUTTON), buttonRectangle, key, layer);
    }

    @Override
    protected void drawAt(Rectangle bounds) {
        super.drawAt(bounds);

        float txtX = (bounds.x + bounds.width / 2f) - txtWidth / 2f;
        float txtY = (bounds.y + 0.05f + bounds.height / 2f) - txtHeight / 2f;
        if (currentTexture == pressedButtonTexture) txtY -= 0.075f;

        Pencil.I().addDrawing(new TextureDrawing(nextRoundTxt,
            txtX, txtY, txtWidth, txtHeight, layer));
    }
}
