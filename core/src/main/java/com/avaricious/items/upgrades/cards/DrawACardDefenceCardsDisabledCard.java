package com.avaricious.items.upgrades.cards;

import com.avaricious.items.upgrades.Hand;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.RunManager;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class DrawACardDefenceCardsDisabledCard extends AbstractCard {

    private final TextureRegion texture = Assets.I().get(AssetKey.EROSION_CARD);

    @Override
    public String description() {
        return "Draw a Card, " + Assets.I().blueText("Defence") + " type cards are disabled this round";
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
        RunManager.I().getRoundsManager().disableDefenceTypeCards();
    }

    @Override
    public Runnable createPopupRunnable(Vector2 pos) {
        return () -> {
        };
    }
}
