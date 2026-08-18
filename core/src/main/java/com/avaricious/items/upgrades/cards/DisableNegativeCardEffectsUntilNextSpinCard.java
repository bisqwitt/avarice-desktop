package com.avaricious.items.upgrades.cards;

import com.avaricious.items.upgrades.IUpgradeType;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class DisableNegativeCardEffectsUntilNextSpinCard extends AbstractCard {
    @Override
    public String description() {
        return "";
    }

    @Override
    protected void onApply() {

    }

    @Override
    public IUpgradeType type() {
        return CardType.UTILITY;
    }

    @Override
    public TextureRegion texture() {
        return null;
    }

    @Override
    public Runnable createPopupRunnable(Vector2 pos) {
        return null;
    }
}
