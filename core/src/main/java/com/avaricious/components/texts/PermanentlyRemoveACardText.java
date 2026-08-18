package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;
import java.util.Collections;

public class PermanentlyRemoveACardText {

    private final float SIZE_RATIO = 15f;
    private final float SPACING = 0.1f;
    private final ZIndex Z_INDEX = ZIndex.PACK_OPENING;

    private final FabledWord chooseText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.C_BIG),
            Assets.I().get(AssetKey.H),
            Assets.I().get(AssetKey.O),
            Assets.I().get(AssetKey.O),
            Assets.I().get(AssetKey.S),
            Assets.I().get(AssetKey.E)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.C_BIG_SHADOW),
            Assets.I().get(AssetKey.H_SHADOW),
            Assets.I().get(AssetKey.O_SHADOW),
            Assets.I().get(AssetKey.O_SHADOW),
            Assets.I().get(AssetKey.S_SHADOW),
            Assets.I().get(AssetKey.E_SHADOW)
        ), new Vector2(0.65f, 17.5f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord aText = new FabledWord(
        Collections.singletonList(Assets.I().get(AssetKey.A)),
        Collections.singletonList(Assets.I().get(AssetKey.A_SHADOW)),
        new Vector2(4.15f, 17.5f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord cardText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.C),
            Assets.I().get(AssetKey.A),
            Assets.I().get(AssetKey.R),
            Assets.I().get(AssetKey.D)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.C_SHADOW),
            Assets.I().get(AssetKey.A_SHADOW),
            Assets.I().get(AssetKey.R_SHADOW),
            Assets.I().get(AssetKey.D_SHADOW)
        ), new Vector2(4.95f, 17.5f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord toText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.T),
            Assets.I().get(AssetKey.O)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.T_SHADOW),
            Assets.I().get(AssetKey.O_SHADOW)
        ), new Vector2(7.2f, 17.5f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord permanentlyText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.P),
            Assets.I().get(AssetKey.E),
            Assets.I().get(AssetKey.R),
            Assets.I().get(AssetKey.M),
            Assets.I().get(AssetKey.A),
            Assets.I().get(AssetKey.N),
            Assets.I().get(AssetKey.E),
            Assets.I().get(AssetKey.N),
            Assets.I().get(AssetKey.T),
            Assets.I().get(AssetKey.L),
            Assets.I().get(AssetKey.Y)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.P_SHADOW),
            Assets.I().get(AssetKey.E_SHADOW),
            Assets.I().get(AssetKey.R_SHADOW),
            Assets.I().get(AssetKey.M_SHADOW),
            Assets.I().get(AssetKey.A_SHADOW),
            Assets.I().get(AssetKey.N_SHADOW),
            Assets.I().get(AssetKey.E_SHADOW),
            Assets.I().get(AssetKey.N_SHADOW),
            Assets.I().get(AssetKey.T_SHADOW),
            Assets.I().get(AssetKey.L_SHADOW),
            Assets.I().get(AssetKey.Y_SHADOW)
        ), new Vector2(0.35f, 16.5f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord removeText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.R),
            Assets.I().get(AssetKey.E),
            Assets.I().get(AssetKey.M),
            Assets.I().get(AssetKey.O),
            Assets.I().get(AssetKey.V),
            Assets.I().get(AssetKey.E)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.R_SHADOW),
            Assets.I().get(AssetKey.E_SHADOW),
            Assets.I().get(AssetKey.M_SHADOW),
            Assets.I().get(AssetKey.O_SHADOW),
            Assets.I().get(AssetKey.V_SHADOW),
            Assets.I().get(AssetKey.E_SHADOW)
        ), new Vector2(5.8f, 16.5f), SIZE_RATIO, SPACING, Z_INDEX
    );

    public void draw(float delta) {
        chooseText.draw(delta);
        aText.draw(delta);
        cardText.draw(delta);
        toText.draw(delta);
        permanentlyText.draw(delta);
        removeText.draw(delta);
    }

}
