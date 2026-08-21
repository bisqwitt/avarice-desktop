package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class CriticalHitChanceText extends FabledText {

    private static final float SIZE_RATIO = 22f;
    private static final float SPACING = 0.05f;

    public CriticalHitChanceText() {
        super(
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.C_BIG),
                    Assets.I().get(AssetKey.R),
                    Assets.I().get(AssetKey.I),
                    Assets.I().get(AssetKey.T),
                    Assets.I().get(AssetKey.I),
                    Assets.I().get(AssetKey.C),
                    Assets.I().get(AssetKey.A),
                    Assets.I().get(AssetKey.L)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.C_BIG_SHADOW),
                    Assets.I().get(AssetKey.R_SHADOW),
                    Assets.I().get(AssetKey.I_SHADOW),
                    Assets.I().get(AssetKey.T_SHADOW),
                    Assets.I().get(AssetKey.I_SHADOW),
                    Assets.I().get(AssetKey.C_SHADOW),
                    Assets.I().get(AssetKey.A_SHADOW),
                    Assets.I().get(AssetKey.L_SHADOW)
                ),
                new Vector2(0f, 0f),
                SIZE_RATIO,
                SPACING,
                ZIndex.SHOP_CARD
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.H_BIG),
                    Assets.I().get(AssetKey.I),
                    Assets.I().get(AssetKey.T)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.H_BIG_SHADOW),
                    Assets.I().get(AssetKey.I_SHADOW),
                    Assets.I().get(AssetKey.T_SHADOW)
                ),
                new Vector2(3.55f, 0f),
                SIZE_RATIO,
                SPACING,
                ZIndex.SHOP_CARD
            )
        );

        setFloatEffects(0.02f, 1f);
    }
}
