package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Seq;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class HealthWord extends FabledWord {
    public HealthWord(Vector2 startingPos, float sizeRatio, float spacing, ZIndex zIndex) {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.H_BIG),
                Assets.I().get(AssetKey.E),
                Assets.I().get(AssetKey.A),
                Assets.I().get(AssetKey.L),
                Assets.I().get(AssetKey.T),
                Assets.I().get(AssetKey.H)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.H_BIG_SHADOW),
                Assets.I().get(AssetKey.E_SHADOW),
                Assets.I().get(AssetKey.A_SHADOW),
                Assets.I().get(AssetKey.L_SHADOW),
                Assets.I().get(AssetKey.T_SHADOW),
                Assets.I().get(AssetKey.H_SHADOW)
            ), startingPos, sizeRatio, spacing, zIndex);
        setColor(Assets.I().healthRedColor());
        Seq.of(floatEffects).forEach(effect -> effect.setStrength(0.02f, 1f));
    }
}
