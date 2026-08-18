package com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;

public class PointsOnCloverCard extends AbstractPointsOnSymbolCard {

    public PointsOnCloverCard() {
        super(Assets.I().get(AssetKey.CLOVER_CARD), Assets.I().get(AssetKey.CLOVER_CARD_COMPLETED));
    }

    @Override
    Symbol symbol() {
        return Symbol.CLOVER;
    }

}
