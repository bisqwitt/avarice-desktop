package com.avaricious.components.popups;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class ApplyPopup extends TextBoxPopup {

    private final TextureRegion applyBox = Assets.I().get(AssetKey.APPLY_TXT_BOX);
    private final TextureRegion applyTxt = Assets.I().get(AssetKey.APPLY_TXT);

    public ApplyPopup(Vector2 pos) {
        super(pos);
    }

    @Override
    public void draw(float delta) {
        super.draw(delta);

        Color applyColor = Assets.I().applyColor();
        Pencil.I().addDrawing(new TextureDrawing(applyTxt,
            calcBoxX() + 0.9f, pos.y + 0.3f, 34 / 20f, 15 / 20f,
            ZIndex.POPUP_DEFAULT, new Color(applyColor.r, applyColor.g, applyColor.b, alpha)));
    }

    @Override
    protected TextureRegion getTextBox() {
        return applyBox;
    }
}
