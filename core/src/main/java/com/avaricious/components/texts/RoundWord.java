package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Seq;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class RoundWord extends FabledWord {

    public RoundWord(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.R_BIG),
                Assets.I().get(AssetKey.O),
                Assets.I().get(AssetKey.U),
                Assets.I().get(AssetKey.N),
                Assets.I().get(AssetKey.D)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.R_BIG_SHADOW),
                Assets.I().get(AssetKey.O_SHADOW),
                Assets.I().get(AssetKey.U_SHADOW),
                Assets.I().get(AssetKey.N_SHADOW),
                Assets.I().get(AssetKey.D_SHADOW)
            ),
            startingPos, sizeRatio, spacing, zIndex
        );
        Seq.of(floatEffects).forEach(effect -> effect.setStrength(0.02f, 1f));
    }
}
