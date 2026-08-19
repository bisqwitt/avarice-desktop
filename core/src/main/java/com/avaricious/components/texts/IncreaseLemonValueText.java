package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class IncreaseLemonValueText extends FabledText {

    private static final float SIZE_RATIO = 25f;
    private static final float SPACING = 0.05f;
    private static final ZIndex Z_INDEX = ZIndex.SHOP_CARD;

    public IncreaseLemonValueText() {
        super(
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.I_BIG),
                    Assets.I().get(AssetKey.N),
                    Assets.I().get(AssetKey.C),
                    Assets.I().get(AssetKey.R),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.A),
                    Assets.I().get(AssetKey.S),
                    Assets.I().get(AssetKey.E)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.I_BIG_SHADOW),
                    Assets.I().get(AssetKey.N_SHADOW),
                    Assets.I().get(AssetKey.C_SHADOW),
                    Assets.I().get(AssetKey.R_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.A_SHADOW),
                    Assets.I().get(AssetKey.S_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW)
                ), new Vector2(1.75f, 5.75f), SIZE_RATIO, SPACING, Z_INDEX
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.L),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.M),
                    Assets.I().get(AssetKey.O),
                    Assets.I().get(AssetKey.N)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.L_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.M_SHADOW),
                    Assets.I().get(AssetKey.O_SHADOW),
                    Assets.I().get(AssetKey.N_SHADOW)
                ), new Vector2(1.75f, 5.15f), SIZE_RATIO, SPACING, Z_INDEX
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.V),
                    Assets.I().get(AssetKey.A),
                    Assets.I().get(AssetKey.L),
                    Assets.I().get(AssetKey.U),
                    Assets.I().get(AssetKey.E)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.V_SHADOW),
                    Assets.I().get(AssetKey.A_SHADOW),
                    Assets.I().get(AssetKey.L_SHADOW),
                    Assets.I().get(AssetKey.U_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW)
                ), new Vector2(3.25f, 5.15f), SIZE_RATIO, SPACING, Z_INDEX
            )
        );
        setFloatEffects(0.02f, 1f);
    }

}
