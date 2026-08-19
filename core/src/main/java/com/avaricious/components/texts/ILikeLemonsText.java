package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;
import java.util.Collections;

public class ILikeLemonsText extends FabledText {

    private static final float SIZE_RATIO = 30f;
    private static final float SPACING = 0.05f;
    private static final ZIndex Z_INDEX = ZIndex.SHOP_CARD;

    public ILikeLemonsText() {
        super(
            new FabledWord(
                Collections.singletonList(
                    Assets.I().get(AssetKey.I_BIG)
                ),
                Collections.singletonList(
                    Assets.I().get(AssetKey.I_BIG_SHADOW)
                ), new Vector2(2.5f, 6f), SIZE_RATIO, SPACING, Z_INDEX
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.L),
                    Assets.I().get(AssetKey.I),
                    Assets.I().get(AssetKey.K),
                    Assets.I().get(AssetKey.E)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.L_SHADOW),
                    Assets.I().get(AssetKey.I_SHADOW),
                    Assets.I().get(AssetKey.K_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW)
                ), new Vector2(2.8f, 6f), SIZE_RATIO, SPACING, Z_INDEX
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.L),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.M),
                    Assets.I().get(AssetKey.O),
                    Assets.I().get(AssetKey.N),
                    Assets.I().get(AssetKey.S)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.L_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.M_SHADOW),
                    Assets.I().get(AssetKey.O_SHADOW),
                    Assets.I().get(AssetKey.N_SHADOW),
                    Assets.I().get(AssetKey.S_SHADOW)
                ), new Vector2(3.75f, 6f), SIZE_RATIO, SPACING, Z_INDEX
            )
        );
    }

}
