package com.avaricious.components.automations;

import com.avaricious.DevTools;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;

public abstract class AbstractAutomation {

    private boolean active;

    public void activate() {
        active = true;
        onActivate();
    }

    protected abstract void onActivate();

    public abstract int price();

    public boolean isActive() {
        return active;
    }

    public boolean isBuyable() {
        return (ScoreDisplay.I().getScoreNumber() >= price()
            || DevTools.unlimitedMoney()) && !isActive();
    }


}
