package com.avaricious.components.buttons;

import com.avaricious.components.slot.BouncingSymbolManager;
import com.avaricious.components.slot.SlotMachine;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;

import java.util.function.BooleanSupplier;

public class SpinButton extends DisablableButton {

    private final BooleanSupplier canSpin;
    private final BooleanSupplier canSpinWithCollectibles;

    public SpinButton(
        Runnable onButtonPressedRunnable,
        BooleanSupplier canSpin,
        BooleanSupplier canSpinWithCollectibles,
        Rectangle buttonRectangle,
        int key
    ) {
        super(onButtonPressedRunnable,
            Assets.I().get(AssetKey.SPIN_BUTTON),
            Assets.I().get(AssetKey.SPIN_BUTTON_PRESSED),
            Assets.I().get(AssetKey.SPIN_BUTTON),
            buttonRectangle, key, ZIndex.BUTTON_BOARD);
        this.canSpin = canSpin;
        this.canSpinWithCollectibles = canSpinWithCollectibles;
    }

    @Override
    public boolean disabled() {
        return !SlotMachine.I().isStale()
            || (!canSpinWithCollectibles.getAsBoolean()
                && BouncingSymbolManager.I().hasUnclaimedCollectibles())
            || !canSpin.getAsBoolean();
    }

    @Override
    protected boolean animateWhenEnabled() {
        return true;
    }
}
