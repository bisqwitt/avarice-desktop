package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Seq;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class SpinsWord extends FabledWord {

    public SpinsWord(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.S_BIG),
                Assets.I().get(AssetKey.P),
                Assets.I().get(AssetKey.I),
                Assets.I().get(AssetKey.N),
                Assets.I().get(AssetKey.S),
                Assets.I().get(AssetKey.COLON)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.S_BIG_SHADOW),
                Assets.I().get(AssetKey.P_SHADOW),
                Assets.I().get(AssetKey.I_SHADOW),
                Assets.I().get(AssetKey.N_SHADOW),
                Assets.I().get(AssetKey.S_SHADOW),
                Assets.I().get(AssetKey.COLON_SHADOW)
            ), startingPos, sizeRatio, spacing, zIndex);
        Seq.of(floatEffects).forEach(effect -> effect.setStrength(0.02f, 1f));
    }

}
