package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class SlotMachineSpeedText extends FabledText {

    private static final float SIZE_RATIO = 22f;
    private static final float SPACING = 0.05f;

    public SlotMachineSpeedText() {
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
                ),
                new Vector2(1.25f, 15f),
                SIZE_RATIO,
                SPACING,
                ZIndex.SHOP_CARD
            ),
            new FabledWord(
                Arrays.asList(
                    Assets.I().get(AssetKey.S_BIG),
                    Assets.I().get(AssetKey.P),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.E),
                    Assets.I().get(AssetKey.D)
                ),
                Arrays.asList(
                    Assets.I().get(AssetKey.S_BIG_SHADOW),
                    Assets.I().get(AssetKey.P_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.E_SHADOW),
                    Assets.I().get(AssetKey.D_SHADOW)
                ),
                new Vector2(2.85f, 15f),
                SIZE_RATIO,
                SPACING,
                ZIndex.SHOP_CARD
            )
        );

        setFloatEffects(0.02f, 1f);
    }
}
