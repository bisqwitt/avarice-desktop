package com.avaricious.items.upgrades.cards;

import com.avaricious.items.upgrades.Hand;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class DrawACardIfLastCard extends AbstractCard implements IConditionalApplyCard {

    private final TextureRegion texture = Assets.I().get(AssetKey.BANNER_CARD);

    @Override
    public String description() {
        return "Draw one Card, can only be played if this is the last card in hand";
    }

    @Override
    protected void onApply() {
        Hand.I().drawCard();
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
        return () -> {
        };
    }

    @Override
    public boolean condition() {
        return Hand.I().getHand().size() == 1;
    }
}
