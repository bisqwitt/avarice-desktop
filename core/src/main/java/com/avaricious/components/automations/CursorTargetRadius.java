package com.avaricious.components.automations;

/** Controls the invisible circular targeting area around the cursor. */
public final class CursorTargetRadius extends AbstractAutomationUpgrade {

    public static final float BASE_RADIUS = 1.35f;
    public static final float RADIUS_PER_UPGRADE = 0.35f;
    public static final int MAX_UPGRADES = 5;

    private int upgradeCount;

    public CursorTargetRadius() {
        super(5_000f, 3f);
        activate();
    }

    @Override
    void onUpgrade() {
        if (!isMaxed()) upgradeCount++;
    }

    @Override
    boolean isMaxed() {
        return upgradeCount >= MAX_UPGRADES;
    }

    public float getRadius() {
        return BASE_RADIUS + upgradeCount * RADIUS_PER_UPGRADE;
    }

    public int getUpgradeCount() {
        return upgradeCount;
    }

    public boolean isMaxRadiusReached() {
        return isMaxed();
    }
}
