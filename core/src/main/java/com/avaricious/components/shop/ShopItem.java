package com.avaricious.components.shop;

import com.avaricious.CreditNumber;
import com.avaricious.components.automations.AbstractAutomation;
import com.avaricious.components.automations.AbstractAutomationUpgrade;
import com.avaricious.components.buttons.BuyAutomationButton;
import com.avaricious.components.buttons.DisablableButton;
import com.avaricious.components.buttons.UpgradeSymbolButton;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.texts.FabledText;
import com.avaricious.components.texts.SymbolDescriptionText;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class ShopItem {

    private final TextureRegion background = Assets.I().get(AssetKey.BLACK_PIXEL);

    private final FabledText title;
    private final FabledText description;
    private final CreditNumber price;
    private final DisablableButton buyButton;

    private float y;

    public ShopItem(FabledText title, AbstractAutomation automation) {
        this(title, null,
            automation.price(),
            new BuyAutomationButton(automation));
    }

    public ShopItem(FabledText title, FabledText description, AbstractAutomationUpgrade automationUpgrade) {
        this(title, description,
            automationUpgrade.price(),
            new BuyAutomationButton(automationUpgrade));

        automationUpgrade.addPriceChangeListener(evt -> {
            updatePrice((int) evt.getNewValue());
        });
    }

    public ShopItem(FabledText title, Symbol symbol) {
        this(title, new SymbolDescriptionText(symbol),
            SymbolValues.I().getPrice(symbol),
            new UpgradeSymbolButton(symbol));

        SymbolValues.I().addPriceChangeListener(evt -> {
            if (evt.getPropertyName().equals(symbol.toString()))
                updatePrice((int) evt.getNewValue());
        });
    }

    private ShopItem(FabledText title, FabledText description, int initialPrice, DisablableButton buyButton) {
        this.title = title;
        this.description = description;
        this.price = new CreditNumber(initialPrice,
            new Rectangle(1.25f, 0, 7 / 24f, 11 / 24f), 0.4f)
            .setZIndex(ZIndex.SHOP_CARD);
        this.buyButton = buyButton;
    }

    public void draw(float delta) {
        Pencil.I().addDrawing(new TextureDrawing(
            background, 0.75f, y, 7.5f, getHeight(), ZIndex.SHOP_CARD, Assets.I().shadowColor()
        ));

        title.draw(delta);
        if (description != null) description.draw(delta);
        price.draw(delta);

        if (buyButton.disabled()) Pencil.I().addDrawing(new TextureDrawing(
            background, 0.75f, y, 7.5f, getHeight(), ZIndex.SHOP_CARD, Assets.I().shadowColor()
        ));
        buyButton.draw(delta);
    }

    public void handleInput(Vector2 mouse, boolean touching, boolean touched) {
        buyButton.handleInput(mouse, touching, touched);
    }

    public float getHeight() {
        return description != null ? 3f : 2.5f;
    }

    public void setY(float y) {
        this.y = y;
        title.setY(y + (description != null ? 2.15f : 1.6f));
        if (description != null) description.setY(y + 1.4f);
        price.getFirstDigitBounds().setY(y + 0.4f);
        buyButton.getBounds().setY(y + 0.3f);
    }

    private void updatePrice(int newPrice) {
        price.setValue(newPrice);
    }

}
