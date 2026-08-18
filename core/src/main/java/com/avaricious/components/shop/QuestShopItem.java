package com.avaricious.components.shop;

import com.avaricious.CreditNumber;
import com.avaricious.components.ItemBag;
import com.avaricious.items.AbstractItem;
import com.avaricious.items.upgrades.quests.AbstractQuest;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class QuestShopItem extends AbstractShopItem {

    private final float ITEM_Y_OFFSET = 0.45f;

    public QuestShopItem(Vector2 initialPos) {
        super(initialPos);
    }

    @Override
    protected void load(Vector2 initialPos) {
        upgrade = AbstractQuest.randomQuest();
        Rectangle bounds = new Rectangle(
            initialPos.x, initialPos.y + ITEM_Y_OFFSET, 48 / 30f, 44 / 30f
        );
        upgrade.addBody(bounds);
        priceTag = new CreditNumber(upgrade.price(), new Rectangle(
            bounds.x + 0.5f, bounds.y, 7 / 20f, 11 / 20f),
            0.4f);
    }

    @Override
    protected void acquireItem() {
        ItemBag.I().addItem(AbstractItem.instantiateItem(upgrade.getClass()));
    }

    @Override
    public void setY(float y) {
        upgrade.getBody().getPos().y = y + ITEM_Y_OFFSET;
    }

    @Override
    protected float getPriceTagYOffset() {
        return 1.8f;
    }

}
