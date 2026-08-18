package com.avaricious.items.upgrades.cards;

import com.avaricious.CreditManager;
import com.avaricious.components.popups.CreditNumberPopup;
import com.avaricious.components.popups.PopupManager;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class OneDollarCard extends AbstractCard {

    private final TextureRegion texture = Assets.I().get(AssetKey.CREDIT_CARD_CARD);

    @Override
    public String description() {
        return Assets.I().yellowText("+1$");
    }

    @Override
    protected void onApply() {
        CreditManager.I().gain(1);
    }

    @Override
    public IUpgradeType type() {
        return CardType.UTILITY;
    }

    @Override
    public TextureRegion texture() {
        return texture;
    }

    @Override
    public Runnable createPopupRunnable(Vector2 pos) {
        return () -> PopupManager.I().spawnNumber(new CreditNumberPopup(1,
            posToBounds(pos), false, false));
    }
}
