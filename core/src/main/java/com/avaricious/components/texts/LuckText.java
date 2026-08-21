package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class LuckText extends FabledText {

    public LuckText() {
        super(new FabledWord(
            Arrays.asList(
                Assets.I().get(AssetKey.L_BIG),
                Assets.I().get(AssetKey.U),
                Assets.I().get(AssetKey.C),
                Assets.I().get(AssetKey.K)
            ),
            Arrays.asList(
                Assets.I().get(AssetKey.L_BIG_SHADOW),
                Assets.I().get(AssetKey.U_SHADOW),
                Assets.I().get(AssetKey.C_SHADOW),
                Assets.I().get(AssetKey.K_SHADOW)
            ),
            new Vector2(1.25f, 15f),
            22f,
            0.05f,
            ZIndex.SHOP_CARD
        ));

        setFloatEffects(0.02f, 1f);
    }
}
