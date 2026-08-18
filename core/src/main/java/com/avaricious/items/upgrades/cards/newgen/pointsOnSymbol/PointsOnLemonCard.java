package com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;

public class PointsOnLemonCard extends AbstractPointsOnSymbolCard {

    public PointsOnLemonCard() {
        super(Assets.I().get(AssetKey.LEMON_CARD), Assets.I().get(AssetKey.LEMON_CARD_COMPLETED));
    }

    @Override
    Symbol symbol() {
        return Symbol.LEMON;
    }

}
