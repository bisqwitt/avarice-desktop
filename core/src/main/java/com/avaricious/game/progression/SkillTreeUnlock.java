package com.avaricious.game.progression;

/** One-time mechanics purchased in the between-round skill tree. */
public enum SkillTreeUnlock {
    TIME_GAIN(5_000f),
    CASH_CHIP_DROP(25_000f),
    CHEST_DROP(50_000f),
    CRITICAL_HIT(10_000f),
    DOUBLE_TRIGGER(50_000f),
    EXTRA_COLLECTIBLE(2_500f);

    private final float price;

    SkillTreeUnlock(float price) {
        this.price = price;
    }

    public float price() {
        return price;
    }
}
