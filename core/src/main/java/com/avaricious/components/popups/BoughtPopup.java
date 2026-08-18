package com.avaricious.components.popups;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class BoughtPopup extends AbstractTextPopup {

    public final static float WIDTH = 55 / 15f;
    public final static float HEIGHT = 14 / 15f;

    public BoughtPopup(Vector2 pos, ZIndex z) {
        super(new Rectangle(pos.x, pos.y, WIDTH, HEIGHT), z);
    }

    @Override
    protected TextureRegion getTexture() {
        return Assets.I().get(AssetKey.BOUGHT_TXT);
    }

    @Override
    protected TextureRegion getShadowTexture() {
        return Assets.I().get(AssetKey.BOUGHT_TXT_SHADOW);
    }
}
