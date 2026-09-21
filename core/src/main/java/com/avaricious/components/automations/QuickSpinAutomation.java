package com.avaricious.components.automations;

/** Allows a new spin to start while collectibles from the last spin remain. */
public final class QuickSpinAutomation extends AbstractAutomation {

    @Override
    protected void onActivate() {
    }

    @Override
    public float price() {
        return 10_000f;
    }
}
