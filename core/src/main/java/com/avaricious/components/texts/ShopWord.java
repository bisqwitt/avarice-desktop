package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class ShopWord extends FabledWord {
    public ShopWord(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.S_BIG),
                Assets.I().get(AssetKey.H),
                Assets.I().get(AssetKey.O),
                Assets.I().get(AssetKey.P)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.S_BIG_SHADOW),
                Assets.I().get(AssetKey.H_SHADOW),
                Assets.I().get(AssetKey.O_SHADOW),
                Assets.I().get(AssetKey.P_SHADOW)
            ), startingPos, sizeRatio, spacing, zIndex);
    }
}
