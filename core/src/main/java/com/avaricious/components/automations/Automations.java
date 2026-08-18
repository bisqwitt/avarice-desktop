package com.avaricious.components.automations;

public class Automations {

    private static Automations instance;

    public static Automations I() {
        return instance == null ? instance = new Automations() : instance;
    }

    private final AutoSpinAutomation autoSpin = new AutoSpinAutomation();
    private final AutoSpinCapacity autoSpinCapacity = new AutoSpinCapacity();
    private final SpinBuyerAutomation spinBuyer = new SpinBuyerAutomation();
    private final SpinBuyerSpeed spinBuyerSpeed = new SpinBuyerSpeed();

    private final SlotMachineSpeed slotMachineSpeed = new SlotMachineSpeed();

    private final HandCapacity handCapacity = new HandCapacity();

    private Automations() {
    }

    public AutoSpinAutomation getAutoSpin() {
        return autoSpin;
    }

    public AutoSpinCapacity getAutoSpinCapacity() {
        return autoSpinCapacity;
    }

    public SpinBuyerAutomation getSpinBuyer() {
        return spinBuyer;
    }

    public SpinBuyerSpeed getSpinBuyerSpeed() {
        return spinBuyerSpeed;
    }

    public SlotMachineSpeed getSlotMachineSpeed() {
        return slotMachineSpeed;
    }

    public HandCapacity getHandCapacity() {
        return handCapacity;
    }
}
