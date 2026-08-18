package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class SlotMachineSpeedText extends FabledText {

    private static final float SIZE_RATIO = 22f;
    private static final float SPACING = 0.05f;
    private static final ZIndex Z_INDEX = ZIndex.SHOP_CARD;

    public SlotMachineSpeedText() {
        super(
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.S_BIG),
                    Assets.I().get(AssetKey.L),
                    Assets.I().get(AssetKey.O),
                    Assets.I().get(AssetKey.T)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.S_BIG_SHADOW),
                    Assets.I().get(AssetKey.L_SHADOW),
                    Assets.I().get(AssetKey.O_SHADOW),
                    Assets.I().get(AssetKey.T_SHADOW)
                ), new Vector2(1.25f, 15f), SIZE_RATIO, SPACING, Z_INDEX
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.M),
                    Assets.I().get(AssetKey.A),
                    Assets.I().get(AssetKey.C),
                    Assets.I().get(AssetKey.H),
                    Assets.I().get(AssetKey.I),
                    Assets.I().get(AssetKey.N),
                    Assets.I().get(AssetKey.E)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.M_SHADOW),
                    Assets.I().get(AssetKey.A_SHADOW),
                    Assets.I().get(AssetKey.C_SHADOW),
                    Assets.I().get(AssetKey.H_SHADOW),
                    Assets.I().get(AssetKey.I_SHADOW),
                    Assets.I().get(AssetKey.N_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW)
                ), new Vector2(3f, 15f), SIZE_RATIO, SPACING, Z_INDEX
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.S),
                    Assets.I().get(AssetKey.P),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.D)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.S_SHADOW),
                    Assets.I().get(AssetKey.P_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.D_SHADOW)
                ), new Vector2(5.5f, 15f), SIZE_RATIO, SPACING, Z_INDEX
            )
        );
    }

}
