package com.avaricious.items.upgrades.cards;

import com.avaricious.items.upgrades.Hand;
import com.avaricious.items.upgrades.IUpgradeType;
import com.avaricious.game.run.RoundsManager;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class DrawACardDisabledUntilTwoCardsPlayedCard extends AbstractCard implements IConditionalApplyCard {

    private final TextureRegion texture = Assets.I().get(AssetKey.BUSINESS_CARD);

    @Override
    public String description() {
        return "Draw a card, is disabled until two cards played\n"
            + "(play two cards first)";
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
    }

    @Override
    public Runnable createPopupRunnable(Vector2 pos) {
        return () -> {
        };
    }

    private int cardsPlayedThisRound(RoundsManager rounds) {
        return rounds.getPlayedCardsThisRound().size();
    }

    @Override
    public boolean condition(RoundsManager rounds) {
        return cardsPlayedThisRound(rounds) >= 2;
    }
}
