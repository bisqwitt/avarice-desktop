package com.avaricious.components.buttons;

import com.avaricious.DevTools;
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
    private Runnable onPurchased = () -> {};

    public UpgradeSymbolButton(Symbol symbol) {
        this(symbol, Input.Keys.SPACE);
    }

    public UpgradeSymbolButton(Symbol symbol, int key) {
        super(
            () -> {
                if (!DevTools.freeShopPurchases()) {
                    ScoreDisplay.I().removeFromScore(SymbolValues.I().getPrice(symbol));
                }
                SymbolValues.I().increaseValue(symbol);
            },
            Assets.I().get(AssetKey.LEVEL_UP_BUTTON),
            Assets.I().get(AssetKey.LEVEL_UP_BUTTON_PRESSED),
            Assets.I().get(AssetKey.LEVEL_UP_BUTTON),
            new Rectangle(5.25f, 0f, 79 / 35f, 25 / 35f),
            key, ZIndex.SHOP_CARD
        );
        this.symbol = symbol;

        setVisibleAnimated(true);
    }

    @Override
    public boolean disabled() {
        return !DevTools.freeShopPurchases()
            && ScoreDisplay.I().getScoreNumber() < SymbolValues.I().getPrice(symbol);
    }

    public void setOnPurchased(Runnable onPurchased) {
        this.onPurchased = onPurchased;
    }

    @Override
    protected void onButtonPressed() {
        super.onButtonPressed();
        onPurchased.run();
    }

}
