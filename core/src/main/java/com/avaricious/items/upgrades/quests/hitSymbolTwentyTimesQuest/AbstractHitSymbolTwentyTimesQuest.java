package com.avaricious.items.upgrades.quests.hitSymbolTwentyTimesQuest;

import com.avaricious.components.slot.Symbol;
import com.avaricious.items.upgrades.quests.AbstractQuest;

public abstract class AbstractHitSymbolTwentyTimesQuest extends AbstractQuest {

    @Override
    public String description() {
        return "Hit " + getSymbol().toString() + "'s 20 times in one Round";
    }

    @Override
    public int price() {
        return 3;
    }

    protected abstract Symbol getSymbol();

}
