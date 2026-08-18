package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class PlayerWord extends FabledWord {
    public PlayerWord(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.P_BIG),
                Assets.I().get(AssetKey.L),
                Assets.I().get(AssetKey.A),
                Assets.I().get(AssetKey.Y),
                Assets.I().get(AssetKey.E),
                Assets.I().get(AssetKey.R)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.P_BIG_SHADOW),
                Assets.I().get(AssetKey.L_SHADOW),
                Assets.I().get(AssetKey.A_SHADOW),
                Assets.I().get(AssetKey.Y_SHADOW),
                Assets.I().get(AssetKey.E_SHADOW),
                Assets.I().get(AssetKey.R_SHADOW)
            ), startingPos, sizeRatio, spacing, zIndex);
    }
}
