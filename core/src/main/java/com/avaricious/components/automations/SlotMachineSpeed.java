package com.avaricious.components.automations;

import com.avaricious.components.slot.SlotMachine;
import com.avaricious.components.slot.SlotMachineResultRunner;
import com.avaricious.utility.Seq;

public class SlotMachineSpeed extends AbstractAutomationUpgrade {

    public SlotMachineSpeed() {
        super(300);
        activate();
    }

    @Override
    void onUpgrade() {
        SlotMachine slotMachine = SlotMachine.I();
        Seq.of(slotMachine.getReels()).forEach(reel -> reel.setSpeed(reel.getSpeed() + 2));
        slotMachine.setReelStopStagger(slotMachine.getReelStopStagger() * 0.5f);
        SlotMachineResultRunner.I().setDefaultDelay(SlotMachineResultRunner.I().getDefaultDelay() - 0.05f);
    }

    @Override
    boolean isMaxed() {
        return false;
    }
}
