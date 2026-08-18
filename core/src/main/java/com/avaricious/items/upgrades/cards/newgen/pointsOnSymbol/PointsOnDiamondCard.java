package com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;

public class PointsOnDiamondCard extends AbstractPointsOnSymbolCard {

    public PointsOnDiamondCard() {
        super(Assets.I().get(AssetKey.DIAMOND_CARD), Assets.I().get(AssetKey.DIAMOND_CARD_COMPLETED));
    }

    @Override
    Symbol symbol() {
        return Symbol.DIAMOND;
    }

}
