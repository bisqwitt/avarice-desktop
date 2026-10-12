package com.avaricious.game.progression;

/** One-time mechanics purchased in the between-round skill tree. */
public enum SkillTreeUnlock {
    HOT_HAND(5),
    LONG_FUSE(6),
    LAST_CALL(8),
    JUGGLING_ACT(11),
    RISING_STAKES(7),
    REDLINE(12),
    TIME_GAIN(6),
    CASH_CHIP_DROP(18),
    CHEST_DROP(20),
    CRITICAL_HIT(6),
    DOUBLE_TRIGGER(9),
    EXTRA_COLLECTIBLE(14),
    MACHINE_CLUB_CLASSIC(7),
    CONTINUOUS_PLAY(9),
    MACHINE_HOUSE_SPECIAL(12),
    MACHINE_GOLD_STANDARD(20),
    MACHINE_HIGH_ROLLER(32),
    MACHINE_BLACK_LABEL(50);

    private final int price;

    SkillTreeUnlock(int price) {
        this.price = price;
    }

    public int price() {
        return price;
    }
}
