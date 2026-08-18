package com.avaricious.components.automations;

import com.avaricious.components.ButtonBoard;
import com.avaricious.components.roundInfoPanel.AutoSpinDisplay;

public class AutoSpinAutomation extends AbstractAutomation {

    @Override
    protected void onActivate() {
        Automations.I().getAutoSpinCapacity().activate();
        ButtonBoard.I().activateAutoSpin();
        AutoSpinDisplay.I().show();
    }

    @Override
    public int price() {
        return 150;
    }

}
