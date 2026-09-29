package com.avaricious.items.upgrades.cards;

import com.avaricious.game.run.RoundsManager;

public interface IConditionalApplyCard {
    boolean condition(RoundsManager rounds);
}
