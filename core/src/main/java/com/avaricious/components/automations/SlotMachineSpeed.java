package com.avaricious.components.automations;

import com.avaricious.components.slot.SlotMachine;
import com.avaricious.components.slot.SlotMachineResultRunner;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class SlotMachineSpeed extends AbstractAutomationUpgrade {

    public static final String SPEED_TIER = "speedTier";
    public static final int INSTANT_SPEED_PERCENT = 0;

    /*
     * Approximate last-reel completion times:
     * 3.66s -> 2.38s -> 1.40s -> 0.73s -> instant.
     * Each purchase removes more waiting than the one before it, while every
     * animated tier still leaves enough time for the reel stops to read.
     */
    private static final SpeedProfile[] PROFILES = {
        new SpeedProfile(100, 14f, 0.10f, 1.00f, 0.36f, 0.72f, 0.40f, 1.00f, false),
        new SpeedProfile(150, 20f, 0.07f, 0.65f, 0.22f, 0.50f, 0.29f, 0.70f, false),
        new SpeedProfile(250, 29f, 0.04f, 0.38f, 0.12f, 0.34f, 0.18f, 0.43f, false),
        new SpeedProfile(500, 42f, 0.015f, 0.19f, 0.06f, 0.22f, 0.09f, 0.20f, false),
        new SpeedProfile(
            INSTANT_SPEED_PERCENT,
            42f,
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
        super(300);
        activate();
        applyProfile();
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
