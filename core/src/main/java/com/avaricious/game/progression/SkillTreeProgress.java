package com.avaricious.game.progression;

import com.avaricious.utility.SeededRandomizer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumSet;

/** Run-scoped skill-tree unlocks and the values owned by those unlocks. */
public final class SkillTreeProgress {

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
