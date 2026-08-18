package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class AutoSpinCapacityText extends FabledText {

    private static final float SIZE_RATIO = 22f;
    private static final float SPACING = 0.05f;
    private static final ZIndex Z_INDEX = ZIndex.SHOP_CARD;

    public AutoSpinCapacityText() {
        super(
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.A_BIG),
                    Assets.I().get(AssetKey.U),
                    Assets.I().get(AssetKey.T),
                    Assets.I().get(AssetKey.O)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.A_BIG_SHADOW),
                    Assets.I().get(AssetKey.U_SHADOW),
                    Assets.I().get(AssetKey.T_SHADOW),
                    Assets.I().get(AssetKey.O_SHADOW)
                ), new Vector2(1.25f, 15f), SIZE_RATIO, SPACING, Z_INDEX
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.S),
                    Assets.I().get(AssetKey.P),
                    Assets.I().get(AssetKey.I),
                    Assets.I().get(AssetKey.N)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.S_SHADOW),
                    Assets.I().get(AssetKey.P_SHADOW),
                    Assets.I().get(AssetKey.I_SHADOW),
                    Assets.I().get(AssetKey.N_SHADOW)
                ), new Vector2(3f, 15f), SIZE_RATIO, SPACING, Z_INDEX
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.C),
                    Assets.I().get(AssetKey.A),
                    Assets.I().get(AssetKey.P),
                    Assets.I().get(AssetKey.A),
                    Assets.I().get(AssetKey.C),
                    Assets.I().get(AssetKey.I),
                    Assets.I().get(AssetKey.T),
                    Assets.I().get(AssetKey.Y)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.C_SHADOW),
                    Assets.I().get(AssetKey.A_SHADOW),
                    Assets.I().get(AssetKey.P_SHADOW),
                    Assets.I().get(AssetKey.A_SHADOW),
                    Assets.I().get(AssetKey.C_SHADOW),
                    Assets.I().get(AssetKey.I_SHADOW),
                    Assets.I().get(AssetKey.T_SHADOW),
                    Assets.I().get(AssetKey.Y_SHADOW)
                ), new Vector2(4.65f, 15f), SIZE_RATIO, SPACING, Z_INDEX
            )
        );
        setFloatEffects(0.02f, 1f);
    }

}
