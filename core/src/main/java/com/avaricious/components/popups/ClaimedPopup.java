package com.avaricious.components.popups;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class ClaimedPopup extends AbstractTextPopup {

    public final static float WIDTH = 57 / 15f;
    public final static float HEIGHT = 11 / 15f;

    public ClaimedPopup(Vector2 pos, ZIndex z) {
        super(new Rectangle(pos.x, pos.y, WIDTH, HEIGHT), z);
    }

    @Override
    protected TextureRegion getTexture() {
        return Assets.I().get(AssetKey.CLAIMED_TXT);
    }

    @Override
    protected TextureRegion getShadowTexture() {
        return Assets.I().get(AssetKey.CLAIMED_TXT_SHADOW);
    }
}
