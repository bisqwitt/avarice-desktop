package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class ExtraSpadeChanceText extends FabledText {

    private static final float SIZE_RATIO = 22f;
    private static final float SPACING = 0.05f;
    private static final ZIndex Z_INDEX = ZIndex.SHOP_CARD;

    public ExtraSpadeChanceText() {
        super(
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.E_BIG),
                    Assets.I().get(AssetKey.X),
                    Assets.I().get(AssetKey.T),
                    Assets.I().get(AssetKey.R),
                    Assets.I().get(AssetKey.A)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.E_BIG_SHADOW),
                    Assets.I().get(AssetKey.X_SHADOW),
                    Assets.I().get(AssetKey.T_SHADOW),
                    Assets.I().get(AssetKey.R_SHADOW),
                    Assets.I().get(AssetKey.A_SHADOW)
                ),
                new Vector2(0f, 0f),
                SIZE_RATIO,
                SPACING,
                Z_INDEX
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.S_BIG),
                    Assets.I().get(AssetKey.P),
                    Assets.I().get(AssetKey.A),
                    Assets.I().get(AssetKey.D),
                    Assets.I().get(AssetKey.E)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.S_BIG_SHADOW),
                    Assets.I().get(AssetKey.P_SHADOW),
                    Assets.I().get(AssetKey.A_SHADOW),
                    Assets.I().get(AssetKey.D_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW)
                ),
                new Vector2(2f, 0f),
                SIZE_RATIO,
                SPACING,
                Z_INDEX
            )
        );

        setFloatEffects(0.02f, 1f);
    }
}
