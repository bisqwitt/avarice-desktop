package com.avaricious.items.upgrades.cards;

import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public enum CardType implements IUpgradeType {
    ATTACK(AssetKey.CARD_TYPE_BOX_RED),
    DEFENCE(AssetKey.CARD_TYPE_BOX_BLUE),
    UTILITY(AssetKey.CARD_TYPE_BOX_GREEN),
    UNKNOWN(AssetKey.UNKNOWN_BOX);

    private final AssetKey typeBoxAssetKey;

    CardType(AssetKey typeBox) {
        typeBoxAssetKey = typeBox;
    }

    @Override
    public TextureRegion getTypeBox() {
        return Assets.I().get(typeBoxAssetKey);
    }
}
