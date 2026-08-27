package com.avaricious.components.shop;

import com.avaricious.components.automations.Automations;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.texts.AutoSpinCapacityDescriptionText;
import com.avaricious.components.texts.FabledText;
import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.components.texts.LuckDescriptionText;
import com.avaricious.components.texts.SlotMachineSpeedDescriptionText;
import com.avaricious.components.texts.XpMultiplierDescriptionText;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.GameplayLayout;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;
import java.util.List;

/** An always-visible, compact version of the shop for moment-to-moment purchases. */
public class QuickShop {
    private static final Rectangle SYMBOL_TAB_BOUNDS = new Rectangle(
        GameplayLayout.HUD_LEFT,
        5.76f + GameplayLayout.HUD_Y_OFFSET,
        1.80f, 0.46f);
    private static final Rectangle AUTOMATION_TAB_BOUNDS = new Rectangle(
        GameplayLayout.HUD_LEFT + 1.92f,
        5.76f + GameplayLayout.HUD_Y_OFFSET,
        1.80f, 0.46f);
    private static final float AUTOMATION_ROW_HEIGHT = 0.72f;
    private static final float AUTOMATION_ROW_GAP = 0.08f;
    private static final float SYMBOL_ROW_HEIGHT = 1.02f;
    private static final float SYMBOL_ROW_GAP = 0.10f;
    private static final Rectangle ITEMS_VIEWPORT = new Rectangle(
        GameplayLayout.HUD_LEFT,
        1.50f + GameplayLayout.HUD_Y_OFFSET,
        GameplayLayout.HUD_WIDTH,
        SYMBOL_TAB_BOUNDS.y - (1.50f + GameplayLayout.HUD_Y_OFFSET) - 0.10f
    );
    private static final float COLLISION_TOP =
        6.75f + GameplayLayout.HUD_Y_OFFSET + 0.025f;
    private static final Rectangle COLLISION_BOUNDS = new Rectangle(
        GameplayLayout.HUD_LEFT,
        ITEMS_VIEWPORT.y,
        GameplayLayout.HUD_WIDTH,
        COLLISION_TOP - ITEMS_VIEWPORT.y
    );

    private final GeneratedFabledText title = new GeneratedFabledText(
        "SHOP", 46f, 0.022f, 0.11f, ZIndex.SHOP_CARD, false);
    private final GeneratedFabledText automationTabText = text("AUTOMATIONS", 39f, 0.025f, 0.12f);
    private final GeneratedFabledText symbolTabText = text("SYMBOLS", 36f, 0.027f, 0.13f);
    private final List<ShopItem> automationItems;
    private final List<ShopItem> symbolItems;

    private enum Tab { AUTOMATIONS, SYMBOLS }
    private Tab selectedTab = Tab.SYMBOLS;
    private Tab pressedTab;
    private final Vector2 blockedMouse = new Vector2(-100f, -100f);
    private float symbolScrollOffset;
    private float symbolScrollTarget;

    public QuickShop() {
        title.setAbsoluteX(GameplayLayout.HUD_LEFT);
        title.getWords().forEach(word -> word.setColor(Assets.I().silver()));
        automationTabText.setAbsoluteX(AUTOMATION_TAB_BOUNDS.x + 0.12f);
        automationTabText.fitWithinWidth(AUTOMATION_TAB_BOUNDS.width - 0.24f);
        symbolTabText.setAbsoluteX(SYMBOL_TAB_BOUNDS.x + 0.12f);
        symbolTabText.fitWithinWidth(SYMBOL_TAB_BOUNDS.width - 0.24f);

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

    public void draw(float delta) {
        updateScroll(delta);
        drawSectionDivider();
        title.setY(6.40f + GameplayLayout.HUD_Y_OFFSET);
        title.draw(delta);
        drawTab(AUTOMATION_TAB_BOUNDS, automationTabText, selectedTab == Tab.AUTOMATIONS, delta);
        drawTab(SYMBOL_TAB_BOUNDS, symbolTabText, selectedTab == Tab.SYMBOLS, delta);

        if (selectedTab == Tab.SYMBOLS) {
            drawSymbolScrollbar();
        }

        Pencil.I().startScissors(
            GameContext.I().viewport.getCamera(),
            GameContext.I().batch.getTransformMatrix(),
            ITEMS_VIEWPORT
        );

        if (selectedTab == Tab.AUTOMATIONS) {
            drawAutomationItems(delta);
        } else {
            drawSymbolItems(delta);
        }

        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.WHITE_PIXEL),
            ITEMS_VIEWPORT.x,
            ITEMS_VIEWPORT.y,
            0.001f,
            0.001f,
            ZIndex.SHOP_CARD_TOUCHING,
            new Color(1f, 1f, 1f, 0f)
        ));
        Pencil.I().endScissors();
    }

    private void drawSectionDivider() {
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.BRIGHT_SLATE_PIXEL),
            GameplayLayout.HUD_LEFT, 6.75f + GameplayLayout.HUD_Y_OFFSET,
            GameplayLayout.HUD_WIDTH, 0.025f,
            ZIndex.SHOP_CARD,
            new Color(1f, 1f, 1f, 0.48f)
        ));
    }

    private void drawTab(Rectangle bounds, FabledText text, boolean selected, float delta) {
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.DARK_SLATE_PIXEL),
            bounds.x, bounds.y, bounds.width, bounds.height,
            ZIndex.SHOP_CARD,
            selected
                ? new Color(1f, 1f, 1f, 0.86f)
                : new Color(0.42f, 0.42f, 0.42f, 0.55f)
        ));
        if (selected) {
            Pencil.I().addDrawing(new TextureDrawing(
                Assets.I().get(AssetKey.YELLOW_PIXEL),
                bounds.x, bounds.y, bounds.width, 0.04f,
                ZIndex.SHOP_CARD
            ));
        }
        text.setY(bounds.y + 0.15f);
        text.draw(delta);
    }

    private void drawAutomationItems(float delta) {
        float x = GameplayLayout.HUD_LEFT;
        float y = 4.86f + GameplayLayout.HUD_Y_OFFSET;
        for (ShopItem item : automationItems) {
            item.setHudRowBounds(new Rectangle(
                x, y, GameplayLayout.HUD_WIDTH, AUTOMATION_ROW_HEIGHT));
            item.draw(delta);
            y -= AUTOMATION_ROW_HEIGHT + AUTOMATION_ROW_GAP;
        }
    }

    private void drawSymbolItems(float delta) {
        float top = ITEMS_VIEWPORT.y + ITEMS_VIEWPORT.height
            - SYMBOL_ROW_HEIGHT + symbolScrollOffset;
        for (int index = 0; index < symbolItems.size(); index++) {
            float y = top - index * (SYMBOL_ROW_HEIGHT + SYMBOL_ROW_GAP);
            symbolItems.get(index).setHudSymbolRowBounds(new Rectangle(
                GameplayLayout.HUD_LEFT,
                y,
                GameplayLayout.HUD_WIDTH,
                SYMBOL_ROW_HEIGHT
            ));
            symbolItems.get(index).draw(delta);
        }
    }

    private void updateScroll(float delta) {
        symbolScrollOffset = Interpolation.fade.apply(
            symbolScrollOffset,
            symbolScrollTarget,
            Math.min(1f, delta * 13f)
        );
        if (Math.abs(symbolScrollOffset - symbolScrollTarget) < 0.002f) {
            symbolScrollOffset = symbolScrollTarget;
        }
    }

    private void drawSymbolScrollbar() {
        float maxScroll = maxSymbolScroll();
        if (maxScroll <= 0f) return;

        float trackX = ITEMS_VIEWPORT.x + ITEMS_VIEWPORT.width + 0.045f;
        float trackWidth = 0.045f;
        float contentHeight = symbolContentHeight();
        float thumbHeight = Math.max(
            0.55f,
            ITEMS_VIEWPORT.height * ITEMS_VIEWPORT.height / contentHeight
        );
        float progress = symbolScrollOffset / maxScroll;
        float thumbY = ITEMS_VIEWPORT.y + ITEMS_VIEWPORT.height - thumbHeight
            - progress * (ITEMS_VIEWPORT.height - thumbHeight);

        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.BLACK_PIXEL),
            trackX,
            ITEMS_VIEWPORT.y,
            trackWidth,
            ITEMS_VIEWPORT.height,
            ZIndex.SHOP_CARD,
            new Color(1f, 1f, 1f, 0.24f)
        ));
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.YELLOW_PIXEL),
            trackX - 0.015f,
            thumbY,
            trackWidth + 0.03f,
            thumbHeight,
            ZIndex.SHOP_CARD
        ));
    }

    public void handleInput(Vector2 mouse, boolean pressed, boolean wasPressed) {
        handleTabInput(mouse, pressed, wasPressed);
        List<ShopItem> visibleItems = selectedTab == Tab.AUTOMATIONS
            ? automationItems
            : symbolItems;
        Vector2 itemMouse = ITEMS_VIEWPORT.contains(mouse) ? mouse : blockedMouse;
        for (ShopItem item : visibleItems) {
            if (item.intersects(ITEMS_VIEWPORT)) {
                item.handleInput(itemMouse, pressed, wasPressed);
            }
        }
    }

    public boolean scroll(float amountY, Vector2 mouse) {
        if (
            selectedTab != Tab.SYMBOLS ||
                amountY == 0f ||
                !ITEMS_VIEWPORT.contains(mouse) ||
                maxSymbolScroll() <= 0f
        ) {
            return false;
        }

        symbolScrollTarget = MathUtils.clamp(
            symbolScrollTarget
                + Math.signum(amountY) * (SYMBOL_ROW_HEIGHT + SYMBOL_ROW_GAP),
            0f,
            maxSymbolScroll()
        );
        return true;
    }

    public Rectangle getCollisionBounds() {
        return new Rectangle(COLLISION_BOUNDS);
    }

    private void handleTabInput(Vector2 mouse, boolean pressed, boolean wasPressed) {
        if (pressed && !wasPressed) {
            if (AUTOMATION_TAB_BOUNDS.contains(mouse)) pressedTab = Tab.AUTOMATIONS;
            else if (SYMBOL_TAB_BOUNDS.contains(mouse)) pressedTab = Tab.SYMBOLS;
            else pressedTab = null;
        } else if (!pressed && wasPressed) {
            if (pressedTab == Tab.AUTOMATIONS && AUTOMATION_TAB_BOUNDS.contains(mouse)) {
                selectedTab = Tab.AUTOMATIONS;
            } else if (pressedTab == Tab.SYMBOLS && SYMBOL_TAB_BOUNDS.contains(mouse)) {
                selectedTab = Tab.SYMBOLS;
            }
            pressedTab = null;
        }
    }

    private int symbolRowCount() {
        return symbolItems.size();
    }

    private float symbolContentHeight() {
        int rows = symbolRowCount();
        return rows * SYMBOL_ROW_HEIGHT
            + Math.max(0, rows - 1) * SYMBOL_ROW_GAP;
    }

    private float maxSymbolScroll() {
        return Math.max(0f, symbolContentHeight() - ITEMS_VIEWPORT.height);
    }

    private ShopItem symbolItem(Symbol symbol, int key) {
        return new ShopItem(cardTitle(symbol.toString()), symbol,
            Assets.I().get(symbol.textureKey()), key);
    }

    private static GeneratedFabledText text(String value, float fontSize, float spacing, float scale) {
        return new GeneratedFabledText(value, fontSize, spacing, scale, ZIndex.SHOP_CARD, true);
    }

    private static FabledText cardTitle(String value) {
        FabledText title = text(value, 38f, 0.031f, 0.15f);
        title.setFloatEffects(0.01f, 1f);
        return title;
    }

    private static FabledText label(String value) {
        return text(value, 34f, 0.028f, 0.13f);
    }
}
