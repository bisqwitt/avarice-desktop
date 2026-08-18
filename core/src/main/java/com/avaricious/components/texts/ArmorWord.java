package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Seq;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class ArmorWord extends FabledWord {
    public ArmorWord(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.A_BIG),
                Assets.I().get(AssetKey.R),
                Assets.I().get(AssetKey.M),
                Assets.I().get(AssetKey.O),
                Assets.I().get(AssetKey.R)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.A_BIG_SHADOW),
                Assets.I().get(AssetKey.R_SHADOW),
                Assets.I().get(AssetKey.M_SHADOW),
                Assets.I().get(AssetKey.O_SHADOW),
                Assets.I().get(AssetKey.R_SHADOW)
            ), startingPos, sizeRatio, spacing, zIndex);
        setColor(Assets.I().silver());
        Seq.of(floatEffects).forEach(effect -> effect.setStrength(0.02f, 1f));
    }
}
