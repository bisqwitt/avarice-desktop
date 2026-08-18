package com.avaricious.items.upgrades.cards.newgen.pointsOnSymbol;

import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;

public class PointsOnIronCard extends AbstractPointsOnSymbolCard {

    public PointsOnIronCard() {
        super(Assets.I().get(AssetKey.IRON_CARD), Assets.I().get(AssetKey.IRON_CARD_COMPLETED));
    }

    @Override
    Symbol symbol() {
        return Symbol.IRON;
    }

}
