package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class ItemsWord extends FabledWord {

    public ItemsWord(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.I_BIG),
                Assets.I().get(AssetKey.T),
                Assets.I().get(AssetKey.E),
                Assets.I().get(AssetKey.M),
                Assets.I().get(AssetKey.S)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.I_BIG_SHADOW),
                Assets.I().get(AssetKey.T_SHADOW),
                Assets.I().get(AssetKey.E_SHADOW),
                Assets.I().get(AssetKey.M_SHADOW),
                Assets.I().get(AssetKey.S_SHADOW)
            ), startingPos, sizeRatio, spacing, zIndex);
    }
}
