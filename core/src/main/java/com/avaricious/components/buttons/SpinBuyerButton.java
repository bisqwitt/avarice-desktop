package com.avaricious.components.buttons;

import com.avaricious.components.automations.Automations;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;

public class SpinBuyerButton extends DisablableButton {

    public SpinBuyerButton() {
        super(() -> Automations.I().getSpinBuyer().activate(),
            Assets.I().get(AssetKey.BUY_BUTTON),
            Assets.I().get(AssetKey.BUY_BUTTON_PRESSED),
            Assets.I().get(AssetKey.BUY_BUTTON),
            new Rectangle(5.25f, 13.8f, 79 / 30f, 25 / 30f),
            Input.Keys.SPACE, ZIndex.SHOP);
        setVisibleAnimated(true);
    }

    @Override
    public boolean disabled() {
        return Automations.I().getSpinBuyer().isActive();
    }
}
