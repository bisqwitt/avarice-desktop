package com.avaricious.components.automations;

public class SpinBuyerSpeed extends AbstractAutomationUpgrade {

    public SpinBuyerSpeed() {
        super(0);
    }

    @Override
    void onUpgrade() {

    }

    @Override
    boolean isMaxed() {
        return false;
    }

    @Override
    public float price() {
        return 0f;
    }
}
