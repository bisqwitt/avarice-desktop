package com.avaricious.components.buttons;

import com.avaricious.components.automations.Automations;
import com.avaricious.components.roundInfoPanel.AutoSpinDisplay;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;

public class BuySpinButton extends DisablableButton {

    public BuySpinButton(Runnable onButtonPressedRunnable, Rectangle buttonRectangle, int key) {
        super(onButtonPressedRunnable,
            Assets.I().get(AssetKey.BUY_SPIN_BUTTON),
            Assets.I().get(AssetKey.BUY_SPIN_BUTTON_PRESSED),
            Assets.I().get(AssetKey.BUY_SPIN_BUTTON),
            buttonRectangle, key, ZIndex.BUTTON_BOARD);
    }

    @Override
    public boolean disabled() {
        return ScoreDisplay.I().getScoreNumber() < 50 || AutoSpinDisplay.I().getSpins() == Automations.I().getAutoSpinCapacity().getCapacity();
    }
}
