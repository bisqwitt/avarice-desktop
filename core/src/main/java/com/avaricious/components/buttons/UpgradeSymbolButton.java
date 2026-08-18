package com.avaricious.components.buttons;

import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.slot.Symbol;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;

public class UpgradeSymbolButton extends DisablableButton {

    private final Symbol symbol;

    public UpgradeSymbolButton(Symbol symbol) {
        super(
            () -> {
                ScoreDisplay.I().removeFromScore(SymbolValues.I().getPrice(symbol));
                SymbolValues.I().increaseValue(symbol);
            },
            Assets.I().get(AssetKey.UPGRADE_BUTTON),
            Assets.I().get(AssetKey.UPGRADE_BUTTON_PRESSED),
            Assets.I().get(AssetKey.UPGRADE_BUTTON),
            new Rectangle(5.25f, 0f, 79 / 35f, 25 / 35f),
            Input.Keys.SPACE, ZIndex.SHOP_CARD
        );
        this.symbol = symbol;

        setVisibleAnimated(true);
    }

    @Override
    public boolean disabled() {
        return ScoreDisplay.I().getScoreNumber() < SymbolValues.I().getPrice(symbol);
    }

}
