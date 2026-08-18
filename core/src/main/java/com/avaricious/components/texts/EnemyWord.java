package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class EnemyWord extends FabledWord {
    public EnemyWord(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.E_BIG),
                Assets.I().get(AssetKey.N),
                Assets.I().get(AssetKey.E),
                Assets.I().get(AssetKey.M),
                Assets.I().get(AssetKey.Y)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.E_BIG_SHADOW),
                Assets.I().get(AssetKey.N_SHADOW),
                Assets.I().get(AssetKey.E_SHADOW),
                Assets.I().get(AssetKey.M_SHADOW),
                Assets.I().get(AssetKey.Y_SHADOW)
            ), startingPos, sizeRatio, spacing, zIndex);
    }
}
