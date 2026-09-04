package com.avaricious.components.automations;

import com.avaricious.components.slot.pattern.PatternUnlocks;
import com.avaricious.components.slot.pattern.UnlockablePattern;

import java.util.List;

/** Unlocks the next still-locked non-linear symbol pattern. */
public final class PatternUnlockUpgrade extends AbstractAutomationUpgrade {

    private static final float[] UPGRADE_PRICES = {
        100_000f,
        1_000_000f,
        10_000_000f
    };

    public PatternUnlockUpgrade() {
        super(UPGRADE_PRICES[0]);
        activate();
    }

    @Override
    void onUpgrade() {
        List<UnlockablePattern> locked = PatternUnlocks.I().getLockedPatterns();
        if (!locked.isEmpty()) PatternUnlocks.I().unlock(locked.get(0));
    }

    @Override
    boolean isMaxed() {
        return PatternUnlocks.I().getLockedPatterns().isEmpty();
    }

    @Override
    protected float nextPrice(float currentPrice) {
        int index = Math.min(
            PatternUnlocks.I().getUnlockedCount(),
            UPGRADE_PRICES.length - 1
        );
        return UPGRADE_PRICES[index];
    }
}
