package com.avaricious.components.automations;

import com.avaricious.components.slot.SlotMachine;
import com.avaricious.screens.ScreenManager;
import com.avaricious.screens.SlotScreen;

/** Keeps starting a new spin whenever the previous result has finished. */
public final class FullAutoSpinAutomation extends AbstractAutomation {

    @Override
    protected void onActivate() {
        ScreenManager manager = ScreenManager.I();
        if (manager == null) return;

        SlotScreen screen = manager.getScreen(SlotScreen.class);
        if (
            screen != null &&
                !screen.isShopShowing() &&
                SlotMachine.I().isStale()
        ) {
            screen.onSpinButtonPressed();
        }
    }

    @Override
    public float price() {
        return 2_500_000f;
    }
}
