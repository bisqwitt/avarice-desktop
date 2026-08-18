package com.avaricious.items.upgrades.cards;

import com.avaricious.components.HandUi;
import com.avaricious.items.upgrades.Hand;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class DrawAndDiscardACard extends AbstractCard {

    private final TextureRegion texture = Assets.I().get(AssetKey.PAREIDOLIA);

    @Override
    public String description() {
        return "Draw and discard a card";
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
    protected void onApply() {
        Hand.I().drawCard();
        HandUi.I().selectCardToDiscard();
    }

    @Override
    public Runnable createPopupRunnable(Vector2 pos) {
        return () -> {
        };
    }
}
