package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class LevelUpText extends FabledText {

    private static final float SIZE_RATIO = 15f;
    private static final float SPACING = 0.1f;
    private static final ZIndex Z_INDEX = ZIndex.SHOP_CARD;

    public LevelUpText() {
        super(
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.L_BIG),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.V),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.L)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.L_BIG_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.V_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.L_SHADOW)
                ), new Vector2(6f, 7f), SIZE_RATIO, SPACING, Z_INDEX
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.U),
                    Assets.I().get(AssetKey.P),
                    Assets.I().get(AssetKey.EXCLAMATION)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.U_SHADOW),
                    Assets.I().get(AssetKey.P_SHADOW),
                    Assets.I().get(AssetKey.EXCLAMATION_SHADOW)
                ), new Vector2(8.75f, 7f), SIZE_RATIO, SPACING, Z_INDEX
            )
        );
    }

}
