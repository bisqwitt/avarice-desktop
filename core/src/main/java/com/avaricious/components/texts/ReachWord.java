package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class ReachWord extends FabledWord {
    public ReachWord(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.R_BIG),
                Assets.I().get(AssetKey.E),
                Assets.I().get(AssetKey.A),
                Assets.I().get(AssetKey.C),
                Assets.I().get(AssetKey.H),
                Assets.I().get(AssetKey.COLON)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.R_BIG_SHADOW),
                Assets.I().get(AssetKey.E_SHADOW),
                Assets.I().get(AssetKey.A_SHADOW),
                Assets.I().get(AssetKey.C_SHADOW),
                Assets.I().get(AssetKey.H_SHADOW),
                Assets.I().get(AssetKey.COLON_SHADOW)
            ), startingPos, sizeRatio, spacing, zIndex);
    }
}
