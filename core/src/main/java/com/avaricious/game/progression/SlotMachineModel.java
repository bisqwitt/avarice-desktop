package com.avaricious.game.progression;

/** A complete machine package: identity, reel timing, luck, and presentation. */
public enum SlotMachineModel {
    BACKROOM_RATTLE(
        "BACKROOM RATTLE", null, 0, 75, 0,
        0.46f, 0.52f, 0.55f
    ),
    CLUB_CLASSIC(
        "CLUB CLASSIC", SkillTreeUnlock.MACHINE_CLUB_CLASSIC, 1, 125, 7,
        0.35f, 0.72f, 0.86f
    ),
    HOUSE_SPECIAL(
        "HOUSE SPECIAL", SkillTreeUnlock.MACHINE_HOUSE_SPECIAL, 3, 210, 14,
        0.38f, 0.82f, 0.58f
    ),
    GOLD_STANDARD(
        "GOLD STANDARD", SkillTreeUnlock.MACHINE_GOLD_STANDARD, 5, 375, 21,
        1.00f, 0.78f, 0.28f
    ),
    HIGH_ROLLER(
        "HIGH ROLLER", SkillTreeUnlock.MACHINE_HIGH_ROLLER, 7, 700, 28,
        0.82f, 0.43f, 0.92f
    ),
    BLACK_LABEL(
        "BLACK LABEL", SkillTreeUnlock.MACHINE_BLACK_LABEL, 9, 0, 35,
        1.00f, 0.28f, 0.18f
    );

    private final String displayName;
    private final SkillTreeUnlock unlock;
    private final int speedProfileTier;
    private final int displaySpeedPercent;
    private final int luckBonusPercent;
    private final float accentRed;
    private final float accentGreen;
    private final float accentBlue;

    SlotMachineModel(
        String displayName,
        SkillTreeUnlock unlock,
        int speedProfileTier,
        int displaySpeedPercent,
        int luckBonusPercent,
        float accentRed,
        float accentGreen,
        float accentBlue
    ) {
        this.displayName = displayName;
        this.unlock = unlock;
        this.speedProfileTier = speedProfileTier;
        this.displaySpeedPercent = displaySpeedPercent;
        this.luckBonusPercent = luckBonusPercent;
        this.accentRed = accentRed;
        this.accentGreen = accentGreen;
        this.accentBlue = accentBlue;
    }

    public String displayName() {
        return displayName;
    }

    public SkillTreeUnlock unlock() {
        return unlock;
    }

    public int speedProfileTier() {
        return speedProfileTier;
    }

    public int displaySpeedPercent() {
        return displaySpeedPercent;
    }

    public int luckBonusPercent() {
        return luckBonusPercent;
    }

    public float accentRed() {
        return accentRed;
    }

    public float accentGreen() {
        return accentGreen;
    }

    public float accentBlue() {
        return accentBlue;
    }
}
