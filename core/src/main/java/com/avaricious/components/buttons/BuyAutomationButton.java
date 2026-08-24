package com.avaricious.components.buttons;

import com.avaricious.DevTools;
import com.avaricious.components.automations.AbstractAutomation;
import com.avaricious.components.automations.AbstractAutomationUpgrade;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;

public class BuyAutomationButton extends DisablableButton {

    private final AbstractAutomation automation;
    private Runnable onPurchased = () -> {};

    public BuyAutomationButton(AbstractAutomation automation) {
        this(automation, Input.Keys.SPACE);
    }

    public BuyAutomationButton(AbstractAutomation automation, int key) {
        super(() -> {
                if (!DevTools.freeShopPurchases()) {
                    ScoreDisplay.I().removeFromScore(automation.price());
                }
                automation.activate();
            },
            Assets.I().get(AssetKey.BUY_BUTTON),
            Assets.I().get(AssetKey.BUY_BUTTON_PRESSED),
            Assets.I().get(AssetKey.BUY_BUTTON),
            new Rectangle(5.25f, 13.8f, 79 / 35f, 25 / 35f),
            key, ZIndex.SHOP_CARD);

        this.automation = automation;
        setVisibleAnimated(true);
        setDisabledTexture(Assets.I().get(AssetKey.BOUGHT_BUTTON));
    }

    public BuyAutomationButton(AbstractAutomationUpgrade automationUpgrade) {
        this(automationUpgrade, Input.Keys.SPACE);
    }

    public BuyAutomationButton(AbstractAutomationUpgrade automationUpgrade, int key) {
        super(() -> {
                if (!DevTools.freeShopPurchases()) {
                    ScoreDisplay.I().removeFromScore(automationUpgrade.price());
                }
                automationUpgrade.upgrade();
            },
            Assets.I().get(AssetKey.UPGRADE_BUTTON),
            Assets.I().get(AssetKey.UPGRADE_BUTTON_PRESSED),
            Assets.I().get(AssetKey.UPGRADE_BUTTON),
            new Rectangle(5.25f, 13.8f, 79 / 35f, 25 / 35f),
            key, ZIndex.SHOP_CARD);

        this.automation = automationUpgrade;
        setVisibleAnimated(true);
    }

    @Override
    public boolean disabled() {
        return !automation.isBuyable();
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
