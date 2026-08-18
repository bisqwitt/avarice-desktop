package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class CreditWord extends FabledWord {
    public CreditWord(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.C_BIG),
                Assets.I().get(AssetKey.R),
                Assets.I().get(AssetKey.E),
                Assets.I().get(AssetKey.D),
                Assets.I().get(AssetKey.I),
                Assets.I().get(AssetKey.T)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.C_BIG_SHADOW),
                Assets.I().get(AssetKey.R_SHADOW),
                Assets.I().get(AssetKey.E_SHADOW),
                Assets.I().get(AssetKey.D_SHADOW),
                Assets.I().get(AssetKey.I_SHADOW),
                Assets.I().get(AssetKey.T_SHADOW)
            ),
            startingPos, sizeRatio, spacing, zIndex
        );
    }
}
