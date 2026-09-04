package com.avaricious.components.shop;

import com.avaricious.CreditNumber;
import com.avaricious.audio.AudioManager;
import com.avaricious.components.ScreenShake;
import com.avaricious.components.automations.AbstractAutomation;
import com.avaricious.components.automations.AbstractAutomationUpgrade;
import com.avaricious.components.buttons.BuyAutomationButton;
import com.avaricious.components.buttons.DisablableButton;
import com.avaricious.components.buttons.UpgradeSymbolButton;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.texts.FabledText;
import com.avaricious.components.texts.SymbolValueDescription;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class ShopItem {

    private final TextureRegion background = Assets.I().get(AssetKey.BLACK_PIXEL);

    private final FabledText title;
    private final FabledText description;
    private final CreditNumber price;
    private final DisablableButton buyButton;
    private final boolean unavailable;
    private TextureRegion icon;
    private Rectangle cardBounds;
    private Layout layout = Layout.FULL;
    private float iconX;
    private float iconY;
    private float iconSize;
    private float purchaseFeedbackTimer;
    private boolean majorPurchaseFeedback;

    private float y;

    private enum Layout { FULL, COMPACT, HUD_ROW, HUD_SYMBOL_ROW }

    public ShopItem(FabledText title, AbstractAutomation automation) {
        this(title, null,
            automation.price(),
            new BuyAutomationButton(automation));
    }

    public ShopItem(FabledText title, FabledText description, AbstractAutomation automation,
                    TextureRegion icon, int key) {
        this(title, description, automation.price(), new BuyAutomationButton(automation, key));
        this.icon = icon;
        ((BuyAutomationButton) buyButton).setOnPurchased(() -> beginPurchaseFeedback(true));
    }

    public ShopItem(FabledText title, FabledText description, AbstractAutomationUpgrade upgrade,
                    TextureRegion icon, int key) {
        this(title, description, upgrade.price(), new BuyAutomationButton(upgrade, key));
        this.icon = icon;
        upgrade.addPriceChangeListener(evt ->
            updatePrice(((Number) evt.getNewValue()).floatValue()));
        ((BuyAutomationButton) buyButton).setOnPurchased(() -> beginPurchaseFeedback(true));
    }

    public ShopItem(FabledText title, FabledText description, AbstractAutomationUpgrade automationUpgrade) {
        this(title, description,
            automationUpgrade.price(),
            new BuyAutomationButton(automationUpgrade));

        automationUpgrade.addPriceChangeListener(evt -> {
            updatePrice(((Number) evt.getNewValue()).floatValue());
        });
    }

    public ShopItem(FabledText title, Symbol symbol) {
        this(title, new SymbolValueDescription(symbol),
            SymbolValues.I().getPrice(symbol),
            new UpgradeSymbolButton(symbol));

        SymbolValues.I().addPriceChangeListener(evt -> {
            if (evt.getPropertyName().equals(symbol.toString()))
                updatePrice(((Number) evt.getNewValue()).floatValue());
        });
    }

    public ShopItem(FabledText title, Symbol symbol, TextureRegion icon, int key) {
        this(title, new SymbolValueDescription(symbol), SymbolValues.I().getPrice(symbol),
            new UpgradeSymbolButton(symbol, key));
        this.icon = icon;
        SymbolValues.I().addPriceChangeListener(evt -> {
            if (evt.getPropertyName().equals(symbol.toString()))
                updatePrice(((Number) evt.getNewValue()).floatValue());
        });
        ((UpgradeSymbolButton) buyButton).setOnPurchased(() -> beginPurchaseFeedback(false));
    }

    private ShopItem(FabledText title, FabledText description, float initialPrice, DisablableButton buyButton) {
        this.title = title;
        this.description = description;
        this.price = new CreditNumber(initialPrice,
            new Rectangle(1.25f, 0, 7 / 24f, 11 / 24f), 0.4f)
            .setZIndex(ZIndex.SHOP_CARD);
        this.buyButton = buyButton;
        unavailable = buyButton == null;
    }

    public static ShopItem unavailable(
        FabledText title,
        FabledText description,
        TextureRegion icon
    ) {
        ShopItem item = new ShopItem(title, description, 0f, null);
        item.icon = icon;
        return item;
    }

    public void draw(float delta) {
        if (purchaseFeedbackTimer > 0f) {
            purchaseFeedbackTimer = Math.max(0f, purchaseFeedbackTimer - delta);
        }
        if (cardBounds != null) {
            boolean hudLayout = layout == Layout.HUD_ROW || layout == Layout.HUD_SYMBOL_ROW;
            if (!hudLayout) {
                Pencil.I().addDrawing(new TextureDrawing(
                    background, cardBounds.x + 0.10f, cardBounds.y - 0.12f,
                    cardBounds.width, cardBounds.height, ZIndex.SHOP_CARD, Assets.I().shadowColor()
                ));
            }
            Pencil.I().addDrawing(new TextureDrawing(
                Assets.I().get(AssetKey.DARK_SLATE_PIXEL), cardBounds.x, cardBounds.y,
                cardBounds.width, cardBounds.height, ZIndex.SHOP_CARD,
                isDisabled()
                    ? new Color(0.50f, 0.50f, 0.50f, hudLayout ? 0.42f : 1f)
                    : new Color(1f, 1f, 1f, hudLayout ? 0.62f : 1f)
            ));
            if (hudLayout) {
                Pencil.I().addDrawing(new TextureDrawing(
                    Assets.I().get(AssetKey.BRIGHT_SLATE_PIXEL), cardBounds.x, cardBounds.y,
                    cardBounds.width, 0.025f, ZIndex.SHOP_CARD,
                    new Color(1f, 1f, 1f, 0.38f)
                ));
                if (layout == Layout.HUD_SYMBOL_ROW) {
                    Pencil.I().addDrawing(new TextureDrawing(
                        Assets.I().get(AssetKey.BRIGHT_SLATE_PIXEL),
                        cardBounds.x + 2.86f,
                        cardBounds.y + 0.15f,
                        0.018f,
                        cardBounds.height - 0.30f,
                        ZIndex.SHOP_CARD,
                        new Color(1f, 1f, 1f, 0.24f)
                    ));
                }
            } else {
                Pencil.I().addDrawing(new TextureDrawing(
                    Assets.I().get(AssetKey.BRIGHT_SLATE_PIXEL), cardBounds.x,
                    cardBounds.y + cardBounds.height - 0.12f,
                    cardBounds.width, 0.12f, ZIndex.SHOP_CARD
                ));
            }
            if (icon != null) {
                Pencil.I().addDrawing(new TextureDrawing(
                    icon,
                    iconX,
                    iconY,
                    iconSize, iconSize, ZIndex.SHOP_CARD
                ));
            }
        } else {
        Pencil.I().addDrawing(new TextureDrawing(
            background, 0.75f, y, 7.5f, getHeight(), ZIndex.SHOP_CARD, Assets.I().shadowColor()
        ));
        }

        title.draw(delta);
        if (
            description != null &&
                (layout != Layout.HUD_ROW || unavailable)
        ) {
            description.draw(delta);
        }
        if (!unavailable) price.draw(delta);

        if (cardBounds == null && isDisabled()) Pencil.I().addDrawing(new TextureDrawing(
            background, 0.75f, y, 7.5f, getHeight(), ZIndex.SHOP_CARD, Assets.I().shadowColor()
        ));
        if (!unavailable) buyButton.draw(delta);
        drawPurchaseFeedback();
    }

    private void beginPurchaseFeedback(boolean major) {
        majorPurchaseFeedback = major;
        purchaseFeedbackTimer = major ? 0.68f : 0.44f;
        AudioManager.I().playShopPurchase(major);
        ScreenShake.I().addTrauma(major ? 0.20f : 0.085f);
    }

    private void drawPurchaseFeedback() {
        if (purchaseFeedbackTimer <= 0f || cardBounds == null) return;

        float duration = majorPurchaseFeedback ? 0.68f : 0.44f;
        float progress = 1f - purchaseFeedbackTimer / duration;
        float remaining = 1f - progress;
        float flashAlpha = remaining * remaining * (majorPurchaseFeedback ? 0.16f : 0.09f);

        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.WHITE_PIXEL), cardBounds.x, cardBounds.y,
            cardBounds.width, cardBounds.height, ZIndex.SHOP_CARD_TOUCHING,
            new Color(1f, 1f, 1f, flashAlpha)
        ));

        if (icon != null) {
            float baseSize = iconSize;
            float punch = 1f + MathUtils.sin(progress * MathUtils.PI) *
                (majorPurchaseFeedback ? 0.38f : 0.20f);
            float size = baseSize * punch;
            float baseX = iconX;
            float baseY = iconY;
            Pencil.I().addDrawing(new TextureDrawing(
                icon,
                baseX + (baseSize - size) / 2f,
                baseY + (baseSize - size) / 2f,
                size, size, ZIndex.SHOP_CARD_TOUCHING,
                new Color(1f, 1f, 1f, Math.min(1f, remaining * 1.4f))
            ));
        }

        int particleCount = majorPurchaseFeedback ? 18 : 9;
        float radius = MathUtils.lerp(0.12f, majorPurchaseFeedback ? 1.55f : 0.85f, progress);
        float centerX = cardBounds.x + cardBounds.width * 0.5f;
        float centerY = cardBounds.y + cardBounds.height * 0.5f;
        for (int index = 0; index < particleCount; index++) {
            float angle = index * MathUtils.PI2 / particleCount + progress * 0.45f;
            float size = (majorPurchaseFeedback ? 0.11f : 0.075f) * (0.35f + remaining);
            Color color = index % 3 == 0
                ? new Color(1f, 0.76f, 0.20f, remaining)
                : new Color(1f, 1f, 1f, remaining * 0.9f);
            Pencil.I().addDrawing(new TextureDrawing(
                Assets.I().get(AssetKey.WHITE_PIXEL),
                centerX + MathUtils.cos(angle) * radius - size / 2f,
                centerY + MathUtils.sin(angle) * radius - size / 2f,
                size, size, ZIndex.SHOP_CARD_TOUCHING, color
            ));
        }
    }

    public void setBounds(Rectangle bounds) {
        layout = Layout.FULL;
        cardBounds = new Rectangle(bounds);
        y = bounds.y;
        iconX = bounds.x + 0.45f;
        iconY = bounds.y + 0.28f;
        iconSize = 0.82f;
        title.setAbsoluteX(bounds.x + 0.40f);
        title.setY(bounds.y + bounds.height - 0.78f);
        title.fitWithinWidth(bounds.width - 0.80f);
        if (description != null) {
            description.setAbsoluteX(bounds.x + 1.48f);
            description.setY(bounds.y + 0.83f);
            description.fitWithinWidth(2.15f);
        }
        price.getFirstDigitBounds().set(bounds.x + 1.48f, bounds.y + 0.18f, 7 / 24f, 11 / 24f);
        if (!unavailable) {
            buyButton.getBounds().set(bounds.x + bounds.width - 2.35f, bounds.y + 0.14f, 2.00f, 25 / 35f);
        }
    }

    public void setCompactBounds(Rectangle bounds) {
        layout = Layout.COMPACT;
        cardBounds = new Rectangle(bounds);
        y = bounds.y;
        iconX = bounds.x + 0.28f;
        iconY = bounds.y + 0.72f;
        iconSize = 0.72f;
        title.setAbsoluteX(bounds.x + 0.25f);
        title.setY(bounds.y + bounds.height - 0.68f);
        title.fitWithinWidth(bounds.width - 0.5f);
        if (description != null) {
            description.setAbsoluteX(bounds.x + 1.10f);
            description.setY(bounds.y + 0.92f);
            description.fitWithinWidth(bounds.width - 1.35f);
        }
        price.setCompactThreshold(1_000f);
        price.setDigitSpacing(0.20f);
        price.getFirstDigitBounds().set(bounds.x + 0.16f, bounds.y + 0.20f, 0.20f, 11 / 35f);
        if (!unavailable) {
            buyButton.getBounds().set(bounds.x + bounds.width - 1.72f, bounds.y + 0.13f, 1.47f, 25 / 38f);
        }
    }

    public void setHudRowBounds(Rectangle bounds) {
        layout = Layout.HUD_ROW;
        cardBounds = new Rectangle(bounds);
        y = bounds.y;
        iconX = bounds.x + 0.12f;
        iconY = bounds.y + 0.15f;
        iconSize = 0.44f;

        title.setAbsoluteX(bounds.x + 0.70f);
        title.setY(bounds.y + (unavailable ? 0.43f : 0.38f));
        title.fitWithinWidth(bounds.width - (unavailable ? 0.86f : 1.86f));

        if (unavailable && description != null) {
            description.setAbsoluteX(bounds.x + 0.70f);
            description.setY(bounds.y + 0.12f);
            description.fitWithinWidth(bounds.width - 0.86f);
        }

        if (!unavailable) {
            price.setCompactThreshold(1_000f);
            price.setDigitSpacing(0.16f);
            price.getFirstDigitBounds().set(bounds.x + 0.66f, bounds.y + 0.10f, 0.16f, 11 / 42f);
            buyButton.getBounds().set(bounds.x + bounds.width - 0.98f, bounds.y + 0.10f, 0.82f, 0.52f);
        }
    }

    public void setHudSymbolRowBounds(Rectangle bounds) {
        layout = Layout.HUD_SYMBOL_ROW;
        cardBounds = new Rectangle(bounds);
        y = bounds.y;
        iconX = bounds.x + 0.14f;
        iconY = bounds.y + 0.22f;
        iconSize = 0.58f;

        title.setAbsoluteX(bounds.x + 0.86f);
        title.setY(bounds.y + 0.64f);
        title.fitWithinWidth(1.98f);

        if (description != null) {
            description.setAbsoluteX(bounds.x + 0.86f);
            description.setY(bounds.y + 0.22f);
            description.fitWithinWidth(0.72f);
            description.setFloatEffects(0.01f, 1f);
            description.getWords().forEach(word -> word.setColor(Assets.I().silver()));
        }

        price.setCompactThreshold(1_000f);
        price.setDigitSpacing(0.16f);
        price.getFirstDigitBounds().set(
            bounds.x + 1.88f,
            bounds.y + 0.22f,
            0.16f,
            11 / 43f
        );
        if (!unavailable) {
            buyButton.getBounds().set(
                bounds.x + 3.00f,
                bounds.y + 0.22f,
                0.58f,
                0.56f
            );
        }
    }

    public void handleInput(Vector2 mouse, boolean touching, boolean touched) {
        if (!unavailable) buyButton.handleInput(mouse, touching, touched);
    }

    public boolean intersects(Rectangle area) {
        return cardBounds != null && cardBounds.overlaps(area);
    }

    public boolean isDisabled() {
        return unavailable || buyButton.disabled();
    }

    public float getHeight() {
        return description != null ? 3f : 2.5f;
    }

    public void setY(float y) {
        this.y = y;
        title.setY(y + (description != null ? 2.15f : 1.6f));
        if (description != null) description.setY(y + 1.4f);
        price.getFirstDigitBounds().setY(y + 0.4f);
        if (!unavailable) buyButton.getBounds().setY(y + 0.3f);
    }

    private void updatePrice(float newPrice) {
        price.setValue(newPrice);
    }

}
