package com.avaricious.items.upgrades;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.SeededRandomizer;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public enum UpgradeRarity {
    COMMON(AssetKey.RARITY_BOX_COMMON, 45, 0.5f),
    UNCOMMON(AssetKey.RARITY_BOX_UNCOMMON, 30, 1f),
    RARE(AssetKey.RARITY_BOX_RARE, 15, 1.5f),
    EPIC(AssetKey.RARITY_BOX_EPIC, 8, 2f),
    LEGENDARY(AssetKey.RARITY_BOX_LEGENDARY, 2, 3f),
    UNKNOWN(AssetKey.UNKNOWN_BOX, 0, 1f);

    private final TextureRegion rarityBoxTexture;
    private final int levelUpWeight;
    private final float levelUpMultiplier;

    UpgradeRarity(
        AssetKey rarityBox,
        int levelUpWeight,
        float levelUpMultiplier
    ) {
        rarityBoxTexture = Assets.I().get(rarityBox);
        this.levelUpWeight = levelUpWeight;
        this.levelUpMultiplier = levelUpMultiplier;
    }

    public TextureRegion getRarityBoxTexture() {
        return rarityBoxTexture;
    }

    /** Rolls the rarity of one numeric level-up reward. */
    public static UpgradeRarity rollLevelUpRarity() {
        int totalWeight = 0;
        for (UpgradeRarity rarity : values()) {
            totalWeight += rarity.levelUpWeight;
        }

        int roll = SeededRandomizer.nextInt(1, totalWeight);
        for (UpgradeRarity rarity : values()) {
            roll -= rarity.levelUpWeight;
            if (roll <= 0) return rarity;
        }

        return COMMON;
    }

    /**
     * The existing upgrade step is the uncommon baseline. Common rewards are
     * smaller, while rarer rewards scale progressively above it.
     */
    public int scaleLevelUpAmount(int uncommonAmount) {
        return Math.max(1, Math.round(uncommonAmount * levelUpMultiplier));
    }

    public UpgradeRarity next() {
        if (this == LEGENDARY || this == UNKNOWN) return this;

        UpgradeRarity[] values = UpgradeRarity.values();
        int nextIndex = this.ordinal() + 1;

        if (nextIndex >= values.length) {
            return this; // already the highest rarity
        }

        return values[nextIndex];
    }
}
