package com.avaricious.components.automations;

import java.util.function.BooleanSupplier;

/** Keeps starting a new spin whenever the previous result has finished. */
public final class FullAutoSpinAutomation extends AbstractAutomation {

    private Runnable requestSpin = () -> { };
    private BooleanSupplier canRequestSpin = () -> false;

    public void configure(
        Runnable requestSpin,
        BooleanSupplier canRequestSpin
    ) {
        this.requestSpin = requestSpin;
        this.canRequestSpin = canRequestSpin;
    }

    @Override
    protected void onActivate() {
        if (canRequestSpin.getAsBoolean()) {
            requestSpin.run();
        }
    }

    @Override
    public float price() {
        return 2_500_000f;
    }
}
