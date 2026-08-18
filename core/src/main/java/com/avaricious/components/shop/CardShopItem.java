package com.avaricious.components.shop;

import com.avaricious.CreditNumber;
import com.avaricious.items.AbstractItem;
import com.avaricious.items.upgrades.Deck;
import com.avaricious.items.upgrades.cards.AbstractCard;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class CardShopItem extends AbstractShopItem {

    public CardShopItem(Vector2 initialPos) {
        super(initialPos);
    }

    @Override
    protected void load(Vector2 initialPos) {
        upgrade = AbstractCard.randomCard();
        Rectangle bounds = new Rectangle(
            initialPos.x, initialPos.y, AbstractCard.WIDTH / 80f, AbstractCard.HEIGHT / 80f
        );
        upgrade.addBody(bounds);
        priceTag = new CreditNumber(upgrade.price(), new Rectangle(bounds.x + 0.5f, bounds.y, 7 / 20f, 11 / 20f), 0.4f);
    }

    @Override
    protected void acquireItem() {
        Deck.I().addCardToDeck(AbstractItem.instantiateItem(((AbstractCard) upgrade).getClass()));
    }

    @Override
    protected float getPriceTagYOffset() {
        return 2.6f;
    }

}
