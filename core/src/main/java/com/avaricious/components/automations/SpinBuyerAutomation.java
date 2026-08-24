package com.avaricious.components.automations;

public class SpinBuyerAutomation extends AbstractAutomation {
    @Override
    protected void onActivate() {
        Automations.I().getSpinBuyerSpeed().onActivate();
    }

    @Override
    public float price() {
        return 2_000f;
    }
}
