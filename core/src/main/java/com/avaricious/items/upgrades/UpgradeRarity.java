package com.avaricious.items.upgrades;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public enum UpgradeRarity {
    COMMON(AssetKey.RARITY_BOX_COMMON),
    UNCOMMON(AssetKey.RARITY_BOX_UNCOMMON),
    RARE(AssetKey.RARITY_BOX_RARE),
    EPIC(AssetKey.RARITY_BOX_EPIC),
    LEGENDARY(AssetKey.RARITY_BOX_LEGENDARY),
    UNKNOWN(AssetKey.UNKNOWN_BOX);

    private final TextureRegion rarityBoxTexture;

    UpgradeRarity(AssetKey rarityBox) {
        rarityBoxTexture = Assets.I().get(rarityBox);
    }

    public TextureRegion getRarityBoxTexture() {
        return rarityBoxTexture;
    }

    public UpgradeRarity next() {
        UpgradeRarity[] values = UpgradeRarity.values();
        int nextIndex = this.ordinal() + 1;

        if (nextIndex >= values.length) {
            return this; // already the highest rarity
        }

        return values[nextIndex];
    }
}
