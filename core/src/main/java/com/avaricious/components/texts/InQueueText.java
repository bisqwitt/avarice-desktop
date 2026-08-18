package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class InQueueText {

    private final float SIZE_RATIO = 15f;
    private final float SPACING = 0.1f;
    private final ZIndex Z_INDEX = ZIndex.PACK_OPENING;

    private final FabledWord inText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.I_BIG),
            Assets.I().get(AssetKey.N)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.I_BIG_SHADOW),
            Assets.I().get(AssetKey.N_SHADOW)
        ), new Vector2(1.75f, 15f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord queueText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.Q),
            Assets.I().get(AssetKey.U),
            Assets.I().get(AssetKey.E),
            Assets.I().get(AssetKey.U),
            Assets.I().get(AssetKey.E)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.Q_SHADOW),
            Assets.I().get(AssetKey.U_SHADOW),
            Assets.I().get(AssetKey.E_SHADOW),
            Assets.I().get(AssetKey.U_SHADOW),
            Assets.I().get(AssetKey.E_SHADOW)
        ), new Vector2(3.15f, 15f), SIZE_RATIO, SPACING, Z_INDEX
    );

    public void draw(float delta) {
        inText.draw(delta);
        queueText.draw(delta);
    }

}
