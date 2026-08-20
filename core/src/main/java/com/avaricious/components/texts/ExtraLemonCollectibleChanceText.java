package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class ExtraLemonCollectibleChanceText extends FabledText {

    private static final float SIZE_RATIO = 22f;
    private static final float SPACING = 0.05f;
    private static final ZIndex Z_INDEX = ZIndex.SHOP_CARD;

    public ExtraLemonCollectibleChanceText() {
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
//            new FabledWord(
//                Arrays.asList(
//                    Assets.I().get(AssetKey.L_BIG),
//                    Assets.I().get(AssetKey.E),
//                    Assets.I().get(AssetKey.M),
//                    Assets.I().get(AssetKey.O),
//                    Assets.I().get(AssetKey.N)
//                ),
//                Arrays.asList(
//                    Assets.I().get(AssetKey.L_BIG_SHADOW),
//                    Assets.I().get(AssetKey.E_SHADOW),
//                    Assets.I().get(AssetKey.M_SHADOW),
//                    Assets.I().get(AssetKey.O_SHADOW),
//                    Assets.I().get(AssetKey.N_SHADOW)
//                ),
//                new Vector2(2f, 0f),
//                SIZE_RATIO,
//                SPACING,
//                Z_INDEX
//            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.C_BIG),
                    Assets.I().get(AssetKey.O),
                    Assets.I().get(AssetKey.L),
                    Assets.I().get(AssetKey.L),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.C),
                    Assets.I().get(AssetKey.T),
                    Assets.I().get(AssetKey.I),
                    Assets.I().get(AssetKey.B),
                    Assets.I().get(AssetKey.L),
                    Assets.I().get(AssetKey.E)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.C_BIG_SHADOW),
                    Assets.I().get(AssetKey.O_SHADOW),
                    Assets.I().get(AssetKey.L_SHADOW),
                    Assets.I().get(AssetKey.L_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.C_SHADOW),
                    Assets.I().get(AssetKey.T_SHADOW),
                    Assets.I().get(AssetKey.I_SHADOW),
                    Assets.I().get(AssetKey.B_SHADOW),
                    Assets.I().get(AssetKey.L_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW)
                ),
                new Vector2(2f, 0f),
                SIZE_RATIO,
                SPACING,
                Z_INDEX
            )
//            new FabledWord(
//                Arrays.asList(
//                    Assets.I().get(AssetKey.C_BIG),
//                    Assets.I().get(AssetKey.H),
//                    Assets.I().get(AssetKey.A),
//                    Assets.I().get(AssetKey.N),
//                    Assets.I().get(AssetKey.C),
//                    Assets.I().get(AssetKey.E)
//                ),
//                Arrays.asList(
//                    Assets.I().get(AssetKey.C_BIG_SHADOW),
//                    Assets.I().get(AssetKey.H_SHADOW),
//                    Assets.I().get(AssetKey.A_SHADOW),
//                    Assets.I().get(AssetKey.N_SHADOW),
//                    Assets.I().get(AssetKey.C_SHADOW),
//                    Assets.I().get(AssetKey.E_SHADOW)
//                ),
//                new Vector2(8f, 0f),
//                SIZE_RATIO,
//                SPACING,
//                Z_INDEX
//            )
        );

        setFloatEffects(0.02f, 1f);
    }
}
