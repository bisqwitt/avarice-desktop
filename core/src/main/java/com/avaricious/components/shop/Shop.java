package com.avaricious.components.shop;

import com.avaricious.CreditNumber;
import com.avaricious.DevTools;
import com.avaricious.components.ButtonBoard;
import com.avaricious.components.ScreenShake;
import com.avaricious.components.automations.Automations;
import com.avaricious.components.buttons.Button;
import com.avaricious.components.buttons.ExitShopButton;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.texts.*;
import com.avaricious.utility.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;
import java.util.List;

/** Full-screen store dedicated to permanent convenience upgrades. */
public class Shop {
    private static final float WIDTH = 16f;
    private static final float HEIGHT = 9f;
    private static final float CARD_WIDTH = 6.55f;
    private static final float CARD_HEIGHT = 2.20f;
    private static final float AUTOMATION_ROW_STEP = 2.53f;
    private static final int VISIBLE_AUTOMATION_ROWS = 2;
    private static final float SYMBOL_CARD_WIDTH = 3.15f;
    private static final float SYMBOL_CARD_HEIGHT = 2.20f;
    private static final float ENTER_DURATION = 0.28f;

    private final TextureRegion backdrop = Assets.I().get(AssetKey.CHARCOAL_PIXEL_DARKER);
    private final TextureRegion panel = Assets.I().get(AssetKey.CHARCOAL_PIXEL);
    private final GeneratedFabledText title = new GeneratedFabledText(
        "SHOP", 12f, 0.07f, 0.30f, ZIndex.SHOP_CARD, true);
    private final GeneratedFabledText symbolTabText = tabTitle("SYMBOL VALUES");
    private final GeneratedFabledText automationTabText = tabTitle("AUTOMATIONS");
    private final GeneratedFabledText freeShopOnText = developerStatus("FREE SHOP ON", Assets.I().yellow());
    private final GeneratedFabledText freeShopOffText = developerStatus("FREE SHOP OFF", Assets.I().silver());
    private final Rectangle symbolTabBounds = new Rectangle(1.25f, 6.58f, 4.35f, 0.62f);
    private final Rectangle automationTabBounds = new Rectangle(5.78f, 6.58f, 4.35f, 0.62f);
    private final Rectangle automationViewport = new Rectangle(1.10f, 1.20f, 13.70f, 5.13f);
    private final Vector2 blockedMouse = new Vector2(-100f, -100f);
    private final CreditNumber balance;
    private final Button exitButton;
    private final List<ShopItem> automationItems;
    private final List<ShopItem> symbolItems;
    private final Runnable onReturnedFromShop;

    private enum State { HIDDEN, ENTERING, SHOWN, EXITING }
    private enum Tab { AUTOMATIONS, SYMBOL_VALUES }
    private State state = State.HIDDEN;
    private Tab selectedTab = Tab.SYMBOL_VALUES;
    private Tab pressedTab;
    private float transition;
    private float automationScrollOffset;
    private float automationScrollTarget;

    public Shop(Runnable onReturnedFromShop) {
        this.onReturnedFromShop = onReturnedFromShop;
        title.setAbsoluteX(1.2f);
        symbolTabText.setAbsoluteX(symbolTabBounds.x + 0.28f);
        symbolTabText.fitWithinWidth(symbolTabBounds.width - 0.56f);
        automationTabText.setAbsoluteX(automationTabBounds.x + 0.28f);
        automationTabText.fitWithinWidth(automationTabBounds.width - 0.56f);
        freeShopOnText.setAbsoluteX(1.25f);
        freeShopOffText.setAbsoluteX(1.25f);

        balance = new CreditNumber(ScoreDisplay.I().getScoreNumber(),
            new Rectangle(12.0f, 7.72f, 7 / 18f, 11 / 18f), 0.52f)
            .setZIndex(ZIndex.SHOP_CARD);
        ScoreDisplay.I().addScoreChangeListener(evt -> balance.setValue((Float) evt.getNewValue()));
        exitButton = new ExitShopButton(new Rectangle(12.0f, 0.42f, 79 / 27f, 25 / 27f));

        Automations upgrades = Automations.I();
        automationItems = Arrays.asList(
            new ShopItem(cardTitle("AUTO SPIN"), label("HANDS FREE"),
                upgrades.getAutoSpin(), Assets.I().get(AssetKey.SPIN_BUTTON), Input.Keys.NUM_1),
            new ShopItem(cardTitle("AUTO SPIN CAPACITY"), new AutoSpinCapacityDescriptionText(),
                upgrades.getAutoSpinCapacity(), Assets.I().get(AssetKey.SHOPPING_CART), Input.Keys.NUM_2),
            new ShopItem(cardTitle("SLOT SPEED"), new SlotMachineSpeedDescriptionText(),
                upgrades.getSlotMachineSpeed(), Assets.I().get(AssetKey.RETRIGGER), Input.Keys.NUM_3),
            new ShopItem(cardTitle("XP MULTIPLIER"), new XpMultiplierDescriptionText(),
                upgrades.getXpMultiplier(), Assets.I().get(AssetKey.SPADE), Input.Keys.NUM_5),
            new ShopItem(cardTitle("LUCK"), new LuckDescriptionText(),
                upgrades.getLuck(), Assets.I().get(AssetKey.LUCK), Input.Keys.NUM_4)
        );

        symbolItems = Arrays.asList(
            symbolItem(Symbol.LEMON, Input.Keys.NUM_1),
            symbolItem(Symbol.CHERRY, Input.Keys.NUM_2),
            symbolItem(Symbol.CLOVER, Input.Keys.NUM_3),
            symbolItem(Symbol.BELL, Input.Keys.NUM_4),
            symbolItem(Symbol.IRON, Input.Keys.NUM_5),
            symbolItem(Symbol.DIAMOND, Input.Keys.NUM_6),
            symbolItem(Symbol.SEVEN, Input.Keys.NUM_7)
        );
    }

    private ShopItem symbolItem(Symbol symbol, int key) {
        return new ShopItem(cardTitle(symbol.toString()), symbol,
            Assets.I().get(symbol.textureKey()), key);
    }

    private static GeneratedFabledText tabTitle(String text) {
        return new GeneratedFabledText(text, 30f, 0.035f, 0.18f, ZIndex.SHOP_CARD, true);
    }

    private static GeneratedFabledText developerStatus(String text, Color color) {
        GeneratedFabledText status = new GeneratedFabledText(
            text, 42f, 0.025f, 0.12f, ZIndex.SHOP_CARD, true);
        status.getWords().forEach(word -> word.setColor(color));
        return status;
    }

    private FabledText cardTitle(String text) {
        return new GeneratedFabledText(text, 25f, 0.04f, 0.20f, ZIndex.SHOP_CARD, true);
    }

    private FabledText label(String text) {
        return new GeneratedFabledText(text, 30f, 0.035f, 0.16f, ZIndex.SHOP_CARD);
    }

    public void show() {
        if (state != State.HIDDEN) return;
        transition = 0f;
        state = State.ENTERING;
        ButtonBoard.I().moveOut();
    }

    public void exit() {
        if (state == State.HIDDEN || state == State.EXITING) return;
        transition = 1f;
        state = State.EXITING;
        ButtonBoard.I().moveIn();
    }

    public void draw(float delta) {
        if (state == State.HIDDEN) return;
        float eased = Interpolation.pow3Out.apply(transition);
        float offsetY = (1f - eased) * HEIGHT;

        title.setY(7.65f + offsetY);
        title.draw(delta);
        drawTabs(delta, offsetY, false);

        balance.getFirstDigitBounds().setY(7.72f + offsetY);
        exitButton.getBounds().setY(0.38f + offsetY);
        balance.draw(delta);
        exitButton.draw(delta);
        drawDeveloperStatus(delta, offsetY);

        if (selectedTab == Tab.AUTOMATIONS) {
            drawAutomationScrollbar(offsetY);
            drawAutomationItems(delta, offsetY, null);
        } else {
            drawSymbolItems(delta, offsetY, null);
        }
    }

    /** Draws the shop surface and disabled cards into the CRT capture. */
    public void drawPostProcessedItems(float delta) {
        if (state == State.HIDDEN) return;
        update(delta);

        float eased = Interpolation.pow3Out.apply(transition);
        float offsetY = (1f - eased) * HEIGHT;

        Pencil.I().beginPostProcessedOnlyDrawings();
        try {
            Pencil.I().addDrawing(new TextureDrawing(
                backdrop,
                0f,
                0f,
                WIDTH,
                HEIGHT,
                ZIndex.SHOP,
                new Color(1f, 1f, 1f, 0.96f * eased)
            ));
            Pencil.I().addDrawing(new TextureDrawing(
                panel,
                0.65f,
                0.25f + offsetY,
                14.7f,
                8.25f,
                ZIndex.SHOP
            ));

            drawTabs(delta, offsetY, true);

            if (selectedTab == Tab.AUTOMATIONS) {
                drawAutomationItems(delta, offsetY, true);
            } else {
                drawSymbolItems(delta, offsetY, true);
            }
        } finally {
            Pencil.I().endPostProcessedOnlyDrawings();
        }
    }

    private void drawDeveloperStatus(float delta, float offsetY) {
        FabledText status = DevTools.freeShopPurchases()
            ? freeShopOnText
            : freeShopOffText;
        status.setY(0.53f + offsetY);
        status.draw(delta);
    }

    private void drawTabs(
        float delta,
        float offsetY,
        boolean inactiveOnly
    ) {
        symbolTabBounds.y = 6.58f + offsetY;
        automationTabBounds.y = 6.58f + offsetY;

        boolean symbolsSelected = selectedTab == Tab.SYMBOL_VALUES;
        if (inactiveOnly) {
            if (symbolsSelected) {
                drawTab(
                    automationTabBounds,
                    automationTabText,
                    false,
                    delta
                );
            } else {
                drawTab(symbolTabBounds, symbolTabText, false, delta);
            }
            return;
        }

        drawTab(
            symbolTabBounds,
            symbolTabText,
            symbolsSelected,
            symbolsSelected ? delta : 0f
        );
        drawTab(
            automationTabBounds,
            automationTabText,
            !symbolsSelected,
            symbolsSelected ? 0f : delta
        );
    }

    private void drawTab(Rectangle bounds, FabledText text, boolean selected, float delta) {
        Pencil.I().addDrawing(new TextureDrawing(Assets.I().get(AssetKey.BLACK_PIXEL),
            bounds.x + 0.06f, bounds.y - 0.08f, bounds.width, bounds.height,
            ZIndex.SHOP_CARD, Assets.I().shadowColor()));
        Pencil.I().addDrawing(new TextureDrawing(Assets.I().get(AssetKey.DARK_SLATE_PIXEL),
            bounds.x, bounds.y, bounds.width, bounds.height, ZIndex.SHOP_CARD,
            selected ? Color.WHITE : new Color(0.55f, 0.55f, 0.55f, 1f)));
        if (selected) Pencil.I().addDrawing(new TextureDrawing(Assets.I().get(AssetKey.YELLOW_PIXEL),
            bounds.x, bounds.y, bounds.width, 0.08f, ZIndex.SHOP_CARD));
        text.setY(bounds.y + 0.18f);
        text.draw(delta);
    }

    private void drawAutomationItems(
        float delta,
        float offsetY,
        Boolean disabledFilter
    ) {
        float left = 1.25f;
        float right = 8.2f;
        float top = 4.05f + automationScrollOffset + offsetY;
        for (int index = 0; index < automationItems.size(); index++) {
            int row = index / 2;
            float x = index % 2 == 0 ? left : right;
            automationItems.get(index).setBounds(new Rectangle(
                x,
                top - row * AUTOMATION_ROW_STEP,
                CARD_WIDTH,
                CARD_HEIGHT
            ));
        }

        automationViewport.y = 1.20f + offsetY;
        Pencil.I().startScissors(
            GameContext.I().viewport.getCamera(),
            GameContext.I().batch.getTransformMatrix(),
            automationViewport
        );
        for (ShopItem item : automationItems) {
            drawItem(item, delta, disabledFilter);
        }
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.WHITE_PIXEL),
            automationViewport.x,
            automationViewport.y,
            0.001f,
            0.001f,
            ZIndex.SHOP_CARD_TOUCHING,
            new Color(1f, 1f, 1f, 0f)
        ));
        Pencil.I().endScissors();
    }

    private void drawAutomationScrollbar(float offsetY) {
        int rows = automationRowCount();
        if (rows <= VISIBLE_AUTOMATION_ROWS) return;

        float trackX = 14.93f;
        float trackY = 1.45f + offsetY;
        float trackWidth = 0.10f;
        float trackHeight = 4.55f;
        float thumbHeight = Math.max(
            0.65f,
            trackHeight * VISIBLE_AUTOMATION_ROWS / rows
        );
        float progress = maxAutomationScroll() <= 0f
            ? 0f
            : automationScrollOffset / maxAutomationScroll();
        float thumbY = trackY + trackHeight - thumbHeight
            - progress * (trackHeight - thumbHeight);

        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.BLACK_PIXEL),
            trackX,
            trackY,
            trackWidth,
            trackHeight,
            ZIndex.SHOP_CARD,
            new Color(1f, 1f, 1f, 0.22f)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.YELLOW_PIXEL),
            trackX - 0.025f,
            thumbY,
            trackWidth + 0.05f,
            thumbHeight,
            ZIndex.SHOP_CARD
        ));
    }

    private void drawSymbolItems(
        float delta,
        float offsetY,
        Boolean disabledFilter
    ) {
        float gap = 0.28f;
        float topY = 4.05f + offsetY;
        float bottomY = 1.45f + offsetY;
        float topLeft = 1.28f;
        for (int index = 0; index < 4; index++) {
            symbolItems.get(index).setCompactBounds(new Rectangle(
                topLeft + index * (SYMBOL_CARD_WIDTH + gap), topY,
                SYMBOL_CARD_WIDTH, SYMBOL_CARD_HEIGHT));
        }
        float bottomLeft = (WIDTH - (3 * SYMBOL_CARD_WIDTH + 2 * gap)) / 2f;
        for (int index = 0; index < 3; index++) {
            symbolItems.get(index + 4).setCompactBounds(new Rectangle(
                bottomLeft + index * (SYMBOL_CARD_WIDTH + gap), bottomY,
                SYMBOL_CARD_WIDTH, SYMBOL_CARD_HEIGHT));
        }
        for (ShopItem item : symbolItems) {
            drawItem(item, delta, disabledFilter);
        }
    }

    private void drawItem(
        ShopItem item,
        float delta,
        Boolean disabledFilter
    ) {
        boolean disabled = item.isDisabled();
        if (disabledFilter != null && disabled != disabledFilter) return;

        // A disabled item already advanced its animations in the CRT pass.
        item.draw(disabledFilter == null && disabled ? 0f : delta);
    }

    private void update(float delta) {
        automationScrollOffset = Interpolation.fade.apply(
            automationScrollOffset,
            automationScrollTarget,
            Math.min(1f, delta * 13f)
        );
        if (Math.abs(automationScrollOffset - automationScrollTarget) < 0.002f) {
            automationScrollOffset = automationScrollTarget;
        }

        float step = delta / ENTER_DURATION;
        if (state == State.ENTERING) {
            transition = Math.min(1f, transition + step);
            if (transition >= 1f) {
                state = State.SHOWN;
                ScreenShake.I().addTrauma(0.08f);
            }
        } else if (state == State.EXITING) {
            transition = Math.max(0f, transition - step);
            if (transition <= 0f) {
                state = State.HIDDEN;
                onReturnedFromShop.run();
            }
        }
    }

    public void handleInput(Vector2 mouse, boolean pressed, boolean wasPressed, float delta) {
        if (state != State.SHOWN) return;
        handleTabInput(mouse, pressed, wasPressed);
        if (selectedTab == Tab.AUTOMATIONS) {
            Vector2 listMouse = automationViewport.contains(mouse)
                ? mouse
                : blockedMouse;
            for (ShopItem item : automationItems) {
                if (item.intersects(automationViewport)) {
                    item.handleInput(listMouse, pressed, wasPressed);
                }
            }
        } else {
            for (ShopItem item : symbolItems) {
                item.handleInput(mouse, pressed, wasPressed);
            }
        }
        exitButton.handleInput(mouse, pressed, wasPressed);
    }

    public boolean scrollAutomations(float amountY) {
        if (state != State.SHOWN || selectedTab != Tab.AUTOMATIONS || amountY == 0f) {
            return false;
        }

        automationScrollTarget = MathUtils.clamp(
            automationScrollTarget
                + Math.signum(amountY) * AUTOMATION_ROW_STEP,
            0f,
            maxAutomationScroll()
        );
        return true;
    }

    private int automationRowCount() {
        return (automationItems.size() + 1) / 2;
    }

    private float maxAutomationScroll() {
        return Math.max(
            0f,
            (automationRowCount() - VISIBLE_AUTOMATION_ROWS)
                * AUTOMATION_ROW_STEP
        );
    }

    private void handleTabInput(Vector2 mouse, boolean pressed, boolean wasPressed) {
        if (pressed && !wasPressed) {
            if (symbolTabBounds.contains(mouse)) pressedTab = Tab.SYMBOL_VALUES;
            else if (automationTabBounds.contains(mouse)) pressedTab = Tab.AUTOMATIONS;
            else pressedTab = null;
        } else if (!pressed && wasPressed) {
            if (pressedTab == Tab.SYMBOL_VALUES && symbolTabBounds.contains(mouse)) {
                selectedTab = Tab.SYMBOL_VALUES;
            } else if (pressedTab == Tab.AUTOMATIONS && automationTabBounds.contains(mouse)) {
                selectedTab = Tab.AUTOMATIONS;
            }
            pressedTab = null;
        }
    }

    public boolean isShowing() {
        return state != State.HIDDEN;
    }
}
