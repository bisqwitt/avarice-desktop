package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class BellValueText extends FabledText {

    private static final float SIZE_RATIO = 22f;
    private static final float SPACING = 0.05f;
    private static final ZIndex Z_INDEX = ZIndex.SHOP_CARD;

    public BellValueText() {
        super(
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.B_BIG),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.L),
                    Assets.I().get(AssetKey.L)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.B_BIG_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.L_SHADOW),
                    Assets.I().get(AssetKey.L_SHADOW)
                ), new Vector2(1.25f, 0f), SIZE_RATIO, SPACING, Z_INDEX
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
                ), new Vector2(2.65f, 0f), SIZE_RATIO, SPACING, Z_INDEX
            )
        );
    }

}
