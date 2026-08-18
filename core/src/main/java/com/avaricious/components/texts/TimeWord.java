package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Seq;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class TimeWord extends FabledWord {
    public TimeWord(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.T_BIG),
                Assets.I().get(AssetKey.I),
                Assets.I().get(AssetKey.M),
                Assets.I().get(AssetKey.E),
                Assets.I().get(AssetKey.R)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.T_BIG_SHADOW),
                Assets.I().get(AssetKey.I_SHADOW),
                Assets.I().get(AssetKey.M_SHADOW),
                Assets.I().get(AssetKey.E_SHADOW),
                Assets.I().get(AssetKey.R_SHADOW)
            ), startingPos, sizeRatio, spacing, zIndex);
        Seq.of(floatEffects).forEach(effect -> effect.setStrength(0.02f, 1f));
    }
}
