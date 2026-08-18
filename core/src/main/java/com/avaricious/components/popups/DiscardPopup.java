package com.avaricious.components.popups;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class DiscardPopup extends TextBoxPopup {

    private final TextureRegion discardBox = Assets.I().get(AssetKey.DISCARD_TXT_BOX);
    private final TextureRegion discardTxt = Assets.I().get(AssetKey.DISCARD_TXT);

    public DiscardPopup(Vector2 pos) {
        super(pos);
    }

    @Override
    public void draw(float delta) {
        super.draw(delta);

        Color discardColor = Assets.I().healthRedColor();
        Pencil.I().addDrawing(new TextureDrawing(discardTxt,
            calcBoxX() + 0.45f, pos.y + 0.2f, 50 / 20f, 15 / 20f,
            ZIndex.POPUP_DEFAULT, new Color(discardColor.r, discardColor.g, discardColor.b, alpha)));
    }

    @Override
    protected TextureRegion getTextBox() {
        return discardBox;
    }
}
