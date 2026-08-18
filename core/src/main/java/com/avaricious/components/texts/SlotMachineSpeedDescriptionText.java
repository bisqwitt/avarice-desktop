package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class SlotMachineSpeedDescriptionText extends FabledText {

    public SlotMachineSpeedDescriptionText() {
        super(
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.P_BIG),
                    Assets.I().get(AssetKey.L),
                    Assets.I().get(AssetKey.A),
                    Assets.I().get(AssetKey.C),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.H),
                    Assets.I().get(AssetKey.O),
                    Assets.I().get(AssetKey.L),
                    Assets.I().get(AssetKey.D),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.R)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.P_BIG_SHADOW),
                    Assets.I().get(AssetKey.L_SHADOW),
                    Assets.I().get(AssetKey.A_SHADOW),
                    Assets.I().get(AssetKey.C_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.H_SHADOW),
                    Assets.I().get(AssetKey.O_SHADOW),
                    Assets.I().get(AssetKey.L_SHADOW),
                    Assets.I().get(AssetKey.D_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.R_SHADOW)
                ), new Vector2(1.25f, 15), 27f, 0.1f, ZIndex.SHOP_CARD
            )
        );
    }

}
