package com.avaricious.items.upgrades;

import com.avaricious.items.AbstractItem;
import com.avaricious.items.IItemWithRarity;
import com.avaricious.items.IItemWithType;

public abstract class AbstractUpgrade extends AbstractItem implements IItemWithType, IItemWithRarity {

    protected UpgradeRarity rarity = UpgradeRarity.COMMON;

    public UpgradeRarity rarity() {
        return rarity;
    }

}
