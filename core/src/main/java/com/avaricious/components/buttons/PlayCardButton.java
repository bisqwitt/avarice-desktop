package com.avaricious.components.buttons;

import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;

import java.util.function.BooleanSupplier;

public class PlayCardButton extends DisablableButton {

    private final BooleanSupplier cardSelected;

    public PlayCardButton(
        Runnable onButtonPressedRunnable,
        BooleanSupplier cardSelected,
        Rectangle buttonRectangle,
        int key
    ) {
        super(onButtonPressedRunnable,
            Assets.I().get(AssetKey.PLAY_CARD_BUTTON),
            Assets.I().get(AssetKey.PLAY_CARD_BUTTON_PRESSED),
            Assets.I().get(AssetKey.PLAY_CARD_BUTTON),
            buttonRectangle, key, ZIndex.BUTTON_BOARD);
        this.cardSelected = cardSelected;
    }

    @Override
    public boolean disabled() {
        return !cardSelected.getAsBoolean();
    }
}
