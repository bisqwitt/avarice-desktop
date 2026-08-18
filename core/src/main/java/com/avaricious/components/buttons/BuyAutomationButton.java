package com.avaricious.components.buttons;

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

    public BuyAutomationButton(AbstractAutomation automation) {
        super(() -> {
                ScoreDisplay.I().removeFromScore(automation.price());
                automation.activate();
            },
            Assets.I().get(AssetKey.BUY_BUTTON),
            Assets.I().get(AssetKey.BUY_BUTTON_PRESSED),
            Assets.I().get(AssetKey.BUY_BUTTON),
            new Rectangle(5.25f, 13.8f, 79 / 35f, 25 / 35f),
            Input.Keys.SPACE, ZIndex.SHOP_CARD);

        this.automation = automation;
        setVisibleAnimated(true);
        setDisabledTexture(Assets.I().get(AssetKey.BOUGHT_BUTTON));
    }

    public BuyAutomationButton(AbstractAutomationUpgrade automationUpgrade) {
        super(() -> {
                ScoreDisplay.I().removeFromScore(automationUpgrade.price());
                automationUpgrade.upgrade();
            },
            Assets.I().get(AssetKey.UPGRADE_BUTTON),
            Assets.I().get(AssetKey.UPGRADE_BUTTON_PRESSED),
            Assets.I().get(AssetKey.UPGRADE_BUTTON),
            new Rectangle(5.25f, 13.8f, 79 / 35f, 25 / 35f),
            Input.Keys.SPACE, ZIndex.SHOP_CARD);

        this.automation = automationUpgrade;
        setVisibleAnimated(true);
    }

    @Override
    public boolean disabled() {
        return !automation.isBuyable();
    }
}
