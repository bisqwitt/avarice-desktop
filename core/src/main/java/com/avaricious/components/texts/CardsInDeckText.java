package com.avaricious.components.texts;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

public class CardsInDeckText {

    private final float SIZE_RATIO = 15f;
    private final float SPACING = 0.1f;
    private final ZIndex Z_INDEX = ZIndex.UNFOLDED_DECK_CARD;

    private final FabledWord cardsText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.C_BIG),
            Assets.I().get(AssetKey.A),
            Assets.I().get(AssetKey.R),
            Assets.I().get(AssetKey.D),
            Assets.I().get(AssetKey.S)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.C_BIG_SHADOW),
            Assets.I().get(AssetKey.A_SHADOW),
            Assets.I().get(AssetKey.R_SHADOW),
            Assets.I().get(AssetKey.D_SHADOW),
            Assets.I().get(AssetKey.S_SHADOW)
        ), new Vector2(), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord inText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.I),
            Assets.I().get(AssetKey.N)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.I_SHADOW),
            Assets.I().get(AssetKey.N_SHADOW)
        ), new Vector2(), SIZE_RATIO, SPACING, Z_INDEX
    );

    private final FabledWord deckText = new FabledWord(
        Arrays.asList(
            Assets.I().get(AssetKey.D),
            Assets.I().get(AssetKey.E),
            Assets.I().get(AssetKey.C),
            Assets.I().get(AssetKey.K_SHADOW)
        ),
        Arrays.asList(
            Assets.I().get(AssetKey.D_SHADOW),
            Assets.I().get(AssetKey.E_SHADOW),
            Assets.I().get(AssetKey.C_SHADOW),
            Assets.I().get(AssetKey.K_SHADOW)
        ), new Vector2(), SIZE_RATIO, SPACING, Z_INDEX
    );

    public void draw(float delta) {
        cardsText.draw(delta);
        inText.draw(delta);
        deckText.draw(delta);
    }

}
