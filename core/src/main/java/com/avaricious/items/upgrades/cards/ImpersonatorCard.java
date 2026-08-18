package com.avaricious.items.upgrades.cards;

import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class ImpersonatorCard extends AbstractCard {
    @Override
    public String description() {
        return "Select a card and apply its effects";
    }

    @Override
    public TextureRegion texture() {
        return Assets.I().get(AssetKey.MADNESS);
    }

    @Override
    protected void onApply() {

    }

    @Override
    public Runnable createPopupRunnable(Vector2 pos) {
        return null;
    }

    @Override
    public IUpgradeType type() {
        return CardType.UNKNOWN;
    }
}
