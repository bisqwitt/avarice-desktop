package com.avaricious.components;

import com.avaricious.components.texts.FabledWord;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class AvariceWord extends FabledWord {

    public AvariceWord() {
        super(
            Arrays.asList(
                Assets.I().get(AssetKey.A_BIG),
                Assets.I().get(AssetKey.V),
                Assets.I().get(AssetKey.A),
                Assets.I().get(AssetKey.R),
                Assets.I().get(AssetKey.I),
                Assets.I().get(AssetKey.C),
                Assets.I().get(AssetKey.E)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.A_BIG_SHADOW),
                Assets.I().get(AssetKey.V_SHADOW),
                Assets.I().get(AssetKey.A_SHADOW),
                Assets.I().get(AssetKey.R_SHADOW),
                Assets.I().get(AssetKey.I_SHADOW),
                Assets.I().get(AssetKey.C_SHADOW),
                Assets.I().get(AssetKey.E_SHADOW)
            ),
            new Vector2(2f, 16f), 10f, 0.15f, ZIndex.PATTERN_DISPLAY
        );
    }

}
