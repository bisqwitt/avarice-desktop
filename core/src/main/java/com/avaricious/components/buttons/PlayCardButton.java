package com.avaricious.components.buttons;

import com.avaricious.components.HandUi;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;

public class PlayCardButton extends DisablableButton {

    public PlayCardButton(Runnable onButtonPressedRunnable, Rectangle buttonRectangle, int key) {
        super(onButtonPressedRunnable,
            Assets.I().get(AssetKey.PLAY_CARD_BUTTON),
            Assets.I().get(AssetKey.PLAY_CARD_BUTTON_PRESSED),
            Assets.I().get(AssetKey.PLAY_CARD_BUTTON),
            buttonRectangle, key, ZIndex.BUTTON_BOARD);
    }

    @Override
    public boolean disabled() {
//        return !HandUi.I().cardIsSelected() || RunManager.I().getRoundsManager().getRoundTimer().timerEnded();
        return !HandUi.I().cardIsSelected();
    }
}
