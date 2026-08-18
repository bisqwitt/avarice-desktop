package com.avaricious.components.automations;

public class SpinBuyerAutomation extends AbstractAutomation {
    @Override
    protected void onActivate() {
        Automations.I().getSpinBuyerSpeed().onActivate();
    }

    @Override
    public int price() {
        return 2000;
    }
}
