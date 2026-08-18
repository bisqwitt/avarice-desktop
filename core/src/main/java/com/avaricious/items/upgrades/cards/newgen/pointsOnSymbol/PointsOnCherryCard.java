package com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;

public class PointsOnCherryCard extends AbstractPointsOnSymbolCard {

    public PointsOnCherryCard() {
        super(Assets.I().get(AssetKey.CHERRY_CARD), Assets.I().get(AssetKey.CHERRY_CARD_COMPLETED));
    }

    @Override
    Symbol symbol() {
        return Symbol.CHERRY;
    }

}
