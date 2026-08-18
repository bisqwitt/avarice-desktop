package com.avaricious.items.upgrades.quests;

import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public enum QuestType implements IUpgradeType {
    DEFAULT;

    @Override
    public TextureRegion getTypeBox() {
        return Assets.I().get(AssetKey.RARITY_BOX_COMMON);
    }
}
