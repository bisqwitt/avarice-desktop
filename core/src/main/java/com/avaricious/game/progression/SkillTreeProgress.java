package com.avaricious.game.progression;

import com.avaricious.utility.SeededRandomizer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumSet;

/** Run-scoped skill-tree unlocks and the values owned by those unlocks. */
public final class SkillTreeProgress {

    public static final float BASE_STREAK_VALUE_STEP = 0.25f;
    public static final float RISING_STAKES_VALUE_STEP_BONUS = 0.15f;
    public static final float BASE_STREAK_VALUE_LIMIT = 4f;
    public static final float REDLINE_STREAK_VALUE_LIMIT = 6f;
    public static final float LAST_CALL_LIFETIME_THRESHOLD = 0.80f;
    public static final int LAST_CALL_STREAK_GAIN = 3;
    public static final int JUGGLING_ACT_STREAK_BONUS = 1;
    public static final float LONG_FUSE_LIFETIME_BONUS_SECONDS = 1.25f;

    public static final String TIME_GAIN_SECONDS = "timeGainSeconds";
    public static final int TIME_GAIN_CHANCE_PERCENT = 10;
    public static final int BASE_TIME_GAIN_SECONDS = 1;
    public static final int TIME_GAIN_STEP_SECONDS = 1;
    public static final int MAX_TIME_GAIN_SECONDS = 10;

    private final EnumSet<SkillTreeUnlock> unlocked =
        EnumSet.noneOf(SkillTreeUnlock.class);
    private final PropertyChangeSupport changeSupport =
        new PropertyChangeSupport(this);

    private int timeGainSeconds = BASE_TIME_GAIN_SECONDS;

    public boolean unlock(SkillTreeUnlock skill) {
        return unlocked.add(skill);
    }

    public boolean isUnlocked(SkillTreeUnlock skill) {
        return unlocked.contains(skill);
    }

    public float getStreakValueStep() {
        return BASE_STREAK_VALUE_STEP
            + (isUnlocked(SkillTreeUnlock.RISING_STAKES)
                ? RISING_STAKES_VALUE_STEP_BONUS
                : 0f);
    }

    public float getStreakValueLimit() {
        return isUnlocked(SkillTreeUnlock.REDLINE)
            ? REDLINE_STREAK_VALUE_LIMIT
            : BASE_STREAK_VALUE_LIMIT;
    }

    public float getCollectibleLifetimeSeconds(float baseLifetimeSeconds) {
        return baseLifetimeSeconds
            + (isUnlocked(SkillTreeUnlock.LONG_FUSE)
                ? LONG_FUSE_LIFETIME_BONUS_SECONDS
                : 0f);
    }

    public SlotMachineModel getCurrentSlotMachineModel() {
        SlotMachineModel current = SlotMachineModel.BACKROOM_RATTLE;
        for (SlotMachineModel model : SlotMachineModel.values()) {
            if (model.unlock() != null && isUnlocked(model.unlock())) {
                current = model;
            }
        }
        return current;
    }

    public boolean canSpinWithCollectibles() {
        return isUnlocked(SkillTreeUnlock.CONTINUOUS_PLAY);
    }

    public boolean isLastCallWindow(float lifetimeProgress) {
        return isUnlocked(SkillTreeUnlock.LAST_CALL)
            && lifetimeProgress >= LAST_CALL_LIFETIME_THRESHOLD;
    }

    public int getStreakGain(float lifetimeProgress) {
        return getStreakGain(lifetimeProgress, false);
    }

    public int getStreakGain(
        float lifetimeProgress,
        boolean oldestCollectible
    ) {
        int gain = isLastCallWindow(lifetimeProgress)
            ? LAST_CALL_STREAK_GAIN
            : 1;
        if (oldestCollectible
            && isUnlocked(SkillTreeUnlock.JUGGLING_ACT)) {
            gain += JUGGLING_ACT_STREAK_BONUS;
        }
        return gain;
    }

    public boolean rollTimeGain() {
        return isUnlocked(SkillTreeUnlock.TIME_GAIN)
            && SeededRandomizer.get().nextFloat() * 100f
                < TIME_GAIN_CHANCE_PERCENT;
    }

    public int getTimeGainSeconds() {
        return timeGainSeconds;
    }

    public int getNextTimeGainSeconds(int amount) {
        return Math.min(
            MAX_TIME_GAIN_SECONDS,
            timeGainSeconds + Math.max(0, amount)
        );
    }

    public void increaseTimeGainSeconds(int amount) {
        int oldValue = timeGainSeconds;
        timeGainSeconds = getNextTimeGainSeconds(amount);
        changeSupport.firePropertyChange(
            TIME_GAIN_SECONDS,
            oldValue,
            timeGainSeconds
        );
    }

    public boolean isTimeGainMaxed() {
        return timeGainSeconds >= MAX_TIME_GAIN_SECONDS;
    }

    public void restoreTimeGainSeconds(int seconds) {
        int oldValue = timeGainSeconds;
        timeGainSeconds = Math.max(
            BASE_TIME_GAIN_SECONDS,
            Math.min(MAX_TIME_GAIN_SECONDS, seconds)
        );
        changeSupport.firePropertyChange(
            TIME_GAIN_SECONDS,
            oldValue,
            timeGainSeconds
        );
    }

    public void addTimeGainChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(
            TIME_GAIN_SECONDS,
            listener
        );
    }

    public void reset() {
        unlocked.clear();
        restoreTimeGainSeconds(BASE_TIME_GAIN_SECONDS);
    }
}
