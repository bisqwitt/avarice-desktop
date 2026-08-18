package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class WaitingForOpponentToFinishRoundText {

    private final float SIZE_RATIO = 15f;
    private final float SPACING = 0.1f;
    private final ZIndex Z_INDEX = ZIndex.PACK_OPENING;

    private final FabledWord waitingText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.W_BIG),
            Assets.I().get(AssetKey.A),
            Assets.I().get(AssetKey.I),
            Assets.I().get(AssetKey.T),
            Assets.I().get(AssetKey.I),
            Assets.I().get(AssetKey.N),
            Assets.I().get(AssetKey.G)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.W_BIG_SHADOW),
            Assets.I().get(AssetKey.A_SHADOW),
            Assets.I().get(AssetKey.I_SHADOW),
            Assets.I().get(AssetKey.T_SHADOW),
            Assets.I().get(AssetKey.I_SHADOW),
            Assets.I().get(AssetKey.N_SHADOW),
            Assets.I().get(AssetKey.G_SHADOW)
        ), new Vector2(1.95f, 15f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord forText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.F),
            Assets.I().get(AssetKey.O),
            Assets.I().get(AssetKey.R)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.F_SHADOW),
            Assets.I().get(AssetKey.O_SHADOW),
            Assets.I().get(AssetKey.R_SHADOW)
        ), new Vector2(5.7f, 15f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord opponentText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.O),
            Assets.I().get(AssetKey.P),
            Assets.I().get(AssetKey.P),
            Assets.I().get(AssetKey.O),
            Assets.I().get(AssetKey.N),
            Assets.I().get(AssetKey.E),
            Assets.I().get(AssetKey.N),
            Assets.I().get(AssetKey.T)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.O_SHADOW),
            Assets.I().get(AssetKey.P_SHADOW),
            Assets.I().get(AssetKey.P_SHADOW),
            Assets.I().get(AssetKey.O_SHADOW),
            Assets.I().get(AssetKey.N_SHADOW),
            Assets.I().get(AssetKey.E_SHADOW),
            Assets.I().get(AssetKey.N_SHADOW),
            Assets.I().get(AssetKey.T_SHADOW)
        ), new Vector2(0.35f, 13.75f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord toText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.T),
            Assets.I().get(AssetKey.O)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.T_SHADOW),
            Assets.I().get(AssetKey.O_SHADOW)
        ), new Vector2(4.6f, 13.75f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord finishText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.F),
            Assets.I().get(AssetKey.I),
            Assets.I().get(AssetKey.N),
            Assets.I().get(AssetKey.I),
            Assets.I().get(AssetKey.S),
            Assets.I().get(AssetKey.H)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.F_SHADOW),
            Assets.I().get(AssetKey.I_SHADOW),
            Assets.I().get(AssetKey.N_SHADOW),
            Assets.I().get(AssetKey.I_SHADOW),
            Assets.I().get(AssetKey.S_SHADOW),
            Assets.I().get(AssetKey.H_SHADOW)
        ), new Vector2(5.85f, 13.75f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord theirText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.T),
            Assets.I().get(AssetKey.H),
            Assets.I().get(AssetKey.E),
            Assets.I().get(AssetKey.I),
            Assets.I().get(AssetKey.R)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.T_SHADOW),
            Assets.I().get(AssetKey.H_SHADOW),
            Assets.I().get(AssetKey.E_SHADOW),
            Assets.I().get(AssetKey.I_SHADOW),
            Assets.I().get(AssetKey.R_SHADOW)
        ), new Vector2(1.25f, 12.5f), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord roundText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.R),
            Assets.I().get(AssetKey.O),
            Assets.I().get(AssetKey.U),
            Assets.I().get(AssetKey.N),
            Assets.I().get(AssetKey.D)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.R_SHADOW),
            Assets.I().get(AssetKey.O_SHADOW),
            Assets.I().get(AssetKey.U_SHADOW),
            Assets.I().get(AssetKey.N_SHADOW),
            Assets.I().get(AssetKey.D_SHADOW)
        ), new Vector2(3.85f, 12.5f), SIZE_RATIO, SPACING, Z_INDEX
    );

    public void draw(float delta) {
        waitingText.draw(delta);
        forText.draw(delta);
        opponentText.draw(delta);
        toText.draw(delta);
        finishText.draw(delta);
        theirText.draw(delta);
        roundText.draw(delta);
    }

}
