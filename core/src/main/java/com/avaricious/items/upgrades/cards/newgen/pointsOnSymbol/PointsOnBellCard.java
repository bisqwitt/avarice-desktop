package com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;

public class PointsOnBellCard extends AbstractPointsOnSymbolCard {

    public PointsOnBellCard() {
        super(Assets.I().get(AssetKey.BELL_CARD), Assets.I().get(AssetKey.BELL_CARD_COMPLETED));
    }

    @Override
    Symbol symbol() {
        return Symbol.BELL;
    }

}
