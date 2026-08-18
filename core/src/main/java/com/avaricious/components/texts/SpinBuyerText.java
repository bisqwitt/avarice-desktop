package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class SpinBuyerText extends FabledText {

    private static final float SIZE_RATIO = 20f;
    private static final float SPACING = 0.05f;
    private static final ZIndex Z_INDEX = ZIndex.SHOP_CARD;

    public SpinBuyerText() {
        super(
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.S_BIG),
                    Assets.I().get(AssetKey.P),
                    Assets.I().get(AssetKey.I),
                    Assets.I().get(AssetKey.N)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.S_BIG_SHADOW),
                    Assets.I().get(AssetKey.P_SHADOW),
                    Assets.I().get(AssetKey.I_SHADOW),
                    Assets.I().get(AssetKey.N_SHADOW)
                ), new Vector2(1.25f, 14f), SIZE_RATIO, SPACING, Z_INDEX
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.B),
                    Assets.I().get(AssetKey.U),
                    Assets.I().get(AssetKey.Y),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.R)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.B_SHADOW),
                    Assets.I().get(AssetKey.U_SHADOW),
                    Assets.I().get(AssetKey.Y_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.R_SHADOW)
                ), new Vector2(3f, 14f), SIZE_RATIO, SPACING, Z_INDEX
            )
        );
        setFloatEffects(0.02f, 1f);
    }

}
