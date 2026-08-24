package com.avaricious.components.automations;

import com.avaricious.components.slot.SlotMachine;
import com.avaricious.components.slot.SlotMachineResultRunner;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class SlotMachineSpeed extends AbstractAutomationUpgrade {

    public static final String SPEED_TIER = "speedTier";
    public static final int INSTANT_SPEED_PERCENT = 0;
    private static final float[] UPGRADE_PRICES = {
        2_500f,
        10_000f,
        40_000f,
        175_000f,
        800_000f,
        4_000_000f,
        25_000_000f,
        175_000_000f,
        2_000_000_000f
    };

    /*
     * Frequent early speed wins keep the machine feeling responsive while the
     * later tiers preserve Instant as the long-term cathartic endpoint.
     */
    private static final SpeedProfile[] PROFILES = {
        new SpeedProfile(100, 14f, 0.10f, 1.00f, 0.36f, 0.72f, 0.40f, 1.00f, false),
        new SpeedProfile(125, 17f, 0.085f, 0.82f, 0.30f, 0.62f, 0.35f, 0.86f, false),
        new SpeedProfile(160, 20.5f, 0.070f, 0.66f, 0.24f, 0.53f, 0.30f, 0.72f, false),
        new SpeedProfile(210, 24.5f, 0.055f, 0.52f, 0.19f, 0.45f, 0.25f, 0.59f, false),
        new SpeedProfile(280, 29f, 0.042f, 0.40f, 0.145f, 0.37f, 0.20f, 0.47f, false),
        new SpeedProfile(375, 35f, 0.030f, 0.30f, 0.105f, 0.30f, 0.155f, 0.36f, false),
        new SpeedProfile(500, 42f, 0.020f, 0.22f, 0.075f, 0.24f, 0.115f, 0.27f, false),
        new SpeedProfile(700, 50f, 0.012f, 0.15f, 0.050f, 0.19f, 0.080f, 0.19f, false),
        new SpeedProfile(1000, 60f, 0.007f, 0.10f, 0.030f, 0.15f, 0.055f, 0.12f, false),
        new SpeedProfile(
            INSTANT_SPEED_PERCENT,
            60f,
            0f,
            0f,
            0f,
            0f,
            SlotMachineResultRunner.INSTANT_RESULT_STEP_DELAY,
            0f,
            true
        )
    };

    private final PropertyChangeSupport changeSupport =
        new PropertyChangeSupport(this);

    private int tier = 0;

    public SlotMachineSpeed() {
        super(UPGRADE_PRICES[0]);
        activate();
        applyProfile();
    }

    @Override
    protected float nextPrice(float currentPrice) {
        return UPGRADE_PRICES[Math.min(tier, UPGRADE_PRICES.length - 1)];
    }

    @Override
    void onUpgrade() {
        if (isMaxed()) return;

        int oldTier = tier;
        tier++;
        applyProfile();
        changeSupport.firePropertyChange(SPEED_TIER, oldTier, tier);
    }

    @Override
    boolean isMaxed() {
        return tier >= PROFILES.length - 1;
    }

    private void applyProfile() {
        SpeedProfile profile = PROFILES[tier];

        SlotMachine.I().setSpeedProfile(
            profile.reelSpeed,
            profile.reelStartStagger,
            profile.spinHoldDuration,
            profile.reelStopStagger,
            profile.reelStopDuration,
            profile.emptyResultTimeScale,
            profile.instant
        );
        SlotMachineResultRunner.I().setRevealTiming(
            profile.resultStepDelay,
            profile.instant
        );
    }

    public int getSpeedPercent() {
        return PROFILES[tier].displayPercent;
    }

    public int getNextSpeedPercent() {
        return PROFILES[Math.min(tier + 1, PROFILES.length - 1)].displayPercent;
    }

    public boolean isMaxSpeedReached() {
        return isMaxed();
    }

    public void addSpeedTierChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    private static class SpeedProfile {

        private final int displayPercent;
        private final float reelSpeed;
        private final float reelStartStagger;
        private final float spinHoldDuration;
        private final float reelStopStagger;
        private final float reelStopDuration;
        private final float resultStepDelay;
        private final float emptyResultTimeScale;
        private final boolean instant;

        private SpeedProfile(
            int displayPercent,
            float reelSpeed,
            float reelStartStagger,
            float spinHoldDuration,
            float reelStopStagger,
            float reelStopDuration,
            float resultStepDelay,
            float emptyResultTimeScale,
            boolean instant
        ) {
            this.displayPercent = displayPercent;
            this.reelSpeed = reelSpeed;
            this.reelStartStagger = reelStartStagger;
            this.spinHoldDuration = spinHoldDuration;
            this.reelStopStagger = reelStopStagger;
            this.reelStopDuration = reelStopDuration;
            this.resultStepDelay = resultStepDelay;
            this.emptyResultTimeScale = emptyResultTimeScale;
            this.instant = instant;
        }
    }
}
