package com.avaricious.components.shop;

import com.avaricious.components.automations.Automations;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.texts.*;
import com.avaricious.utility.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;
import java.util.List;

/** Always-visible compact shop with Symbols, Stats, and Unlocks tabs. */
public class QuickShop {
    private static final float TAB_WIDTH = 1.20f;
    private static final float TAB_GAP = 0.06f;
    private static final Rectangle SYMBOL_TAB_BOUNDS = new Rectangle(
        GameplayLayout.HUD_LEFT,
        5.76f + GameplayLayout.HUD_Y_OFFSET,
        TAB_WIDTH, 0.46f);
    private static final Rectangle STATS_TAB_BOUNDS = new Rectangle(
        GameplayLayout.HUD_LEFT + TAB_WIDTH + TAB_GAP,
        5.76f + GameplayLayout.HUD_Y_OFFSET,
        TAB_WIDTH, 0.46f);
    private static final Rectangle UNLOCKS_TAB_BOUNDS = new Rectangle(
        GameplayLayout.HUD_LEFT + (TAB_WIDTH + TAB_GAP) * 2f,
        5.76f + GameplayLayout.HUD_Y_OFFSET,
        TAB_WIDTH, 0.46f);
    private static final float UPGRADE_ROW_HEIGHT = 0.72f;
    private static final float UPGRADE_ROW_GAP = 0.08f;
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
    private final GeneratedFabledText symbolTabText = text("SYMBOLS", 39f, 0.025f, 0.12f);
    private final GeneratedFabledText statsTabText = text("STATS", 39f, 0.025f, 0.12f);
    private final GeneratedFabledText unlocksTabText = text("UNLOCKS", 39f, 0.025f, 0.12f);
    private final List<ShopItem> symbolItems;
    private final List<ShopItem> statsItems;
    private final List<ShopItem> unlockItems;

    private enum Tab { SYMBOLS, STATS, UNLOCKS }
    private Tab selectedTab = Tab.SYMBOLS;
    private Tab pressedTab;
    private final Vector2 blockedMouse = new Vector2(-100f, -100f);
    private float symbolScrollOffset;
    private float symbolScrollTarget;
    private float statsScrollOffset;
    private float statsScrollTarget;
    private float unlocksScrollOffset;
    private float unlocksScrollTarget;

    public QuickShop() {
        title.setAbsoluteX(GameplayLayout.HUD_LEFT);
        title.getWords().forEach(word -> word.setColor(Assets.I().silver()));
        positionTabText(symbolTabText, SYMBOL_TAB_BOUNDS);
        positionTabText(statsTabText, STATS_TAB_BOUNDS);
        positionTabText(unlocksTabText, UNLOCKS_TAB_BOUNDS);

        Automations upgrades = Automations.I();
        statsItems = Arrays.asList(
            new ShopItem(cardTitle("SLOT SPEED"), new SlotMachineSpeedDescriptionText(),
                upgrades.getSlotMachineSpeed(), Assets.I().get(AssetKey.RETRIGGER), Input.Keys.NUM_1),
            new ShopItem(cardTitle("XP MULTIPLIER"), new XpMultiplierDescriptionText(),
                upgrades.getXpMultiplier(), Assets.I().get(AssetKey.SPADE), Input.Keys.NUM_2),
            new ShopItem(cardTitle("EXTRA COLLECTIBLE CHANCE"), new ExtraCollectibleChanceDescription(),
                upgrades.getExtraCollectibleChance(), Assets.I().get(AssetKey.RETRIGGER), Input.Keys.NUM_3),
            new ShopItem(cardTitle("EXTRA SPADE CHANCE"), new ExtraSpadeChanceDescription(),
                upgrades.getExtraSpadeChance(), Assets.I().get(AssetKey.SPADE), Input.Keys.NUM_4),
            new ShopItem(cardTitle("CRIT CHANCE"), new CriticalHitChanceDescription(),
                upgrades.getCriticalHitChance(), Assets.I().get(AssetKey.CRITICAL_HIT), Input.Keys.NUM_5),
            new ShopItem(cardTitle("CRIT DAMAGE"), new CriticalDamageDescription(),
                upgrades.getCriticalDamage(), Assets.I().get(AssetKey.MULTI), Input.Keys.NUM_6),
            new ShopItem(cardTitle("DOUBLE HIT CHANCE"), new DoubleHitChanceDescription(),
                upgrades.getDoubleHitChance(), Assets.I().get(AssetKey.RETRIGGER), Input.Keys.NUM_7),
            new ShopItem(cardTitle("CASH CHIP DROP CHANCE"), new CashChipChanceDescription(),
                upgrades.getCashChipChance(), Assets.I().get(AssetKey.POKER_CHIP), Input.Keys.NUM_8),
            new ShopItem(cardTitle("CHEST DROP CHANCE"), new ChestDropChanceDescriptionText(),
                upgrades.getChestDropChance(), Assets.I().get(AssetKey.CHEST_CLOSED), Input.Keys.NUM_9),
            new ShopItem(cardTitle("LUCK"), new LuckDescriptionText(),
                upgrades.getLuck(), Assets.I().get(AssetKey.LUCK), Input.Keys.NUM_0)
        );

        unlockItems = Arrays.asList(
            new ShopItem(cardTitle("SPIN QUEUER"), label("QUEUE SPINS"),
                upgrades.getSpinQueuer(), Assets.I().get(AssetKey.SPIN_BUTTON), Input.Keys.NUM_1),
            new ShopItem(cardTitle("SPIN QUEUER CAPACITY"), new AutoSpinCapacityDescriptionText(),
                upgrades.getAutoSpinCapacity(), Assets.I().get(AssetKey.SHOPPING_CART), Input.Keys.NUM_2),
            new ShopItem(cardTitle("AUTOSPIN"), label("FULLY AUTOMATIC"),
                upgrades.getFullAutoSpin(), Assets.I().get(AssetKey.RETRIGGER), Input.Keys.NUM_3),
            new ShopItem(cardTitle("COLLECTORS"), new CollectorCountDescriptionText(),
                upgrades.getCollectorCapacity(), Assets.I().get(AssetKey.COLLECTOR), Input.Keys.NUM_4),
            ShopItem.unavailable(cardTitle("CHEST OPENER"), label("COMING SOON"),
                Assets.I().get(AssetKey.CHEST_CLOSED)),
            new ShopItem(cardTitle("NEW PATTERNS"), new PatternUnlockDescriptionText(),
                upgrades.getPatternUnlock(), Assets.I().get(AssetKey.PLUS_SYMBOL), Input.Keys.NUM_6)
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

    private static void positionTabText(FabledText text, Rectangle bounds) {
        text.fitWithinWidth(bounds.width - 0.16f);
        text.setAbsoluteX(
            bounds.x + (bounds.width - text.getRenderedWidth()) / 2f
        );
    }

    public void draw(float delta) {
        drawSectionDivider();
        title.setY(6.40f + GameplayLayout.HUD_Y_OFFSET);
        title.draw(delta);
        drawTabs(delta, false);
        drawScrollbar();

        Pencil.I().startScissors(GameContext.I().viewport.getCamera(),
            GameContext.I().batch.getTransformMatrix(), ITEMS_VIEWPORT);
        drawSelectedItems(delta, null);
        Pencil.I().addDrawing(new TextureDrawing(Assets.I().get(AssetKey.WHITE_PIXEL),
            ITEMS_VIEWPORT.x, ITEMS_VIEWPORT.y, 0.001f, 0.001f,
            ZIndex.SHOP_CARD_TOUCHING, new Color(1f, 1f, 1f, 0f)));
        Pencil.I().endScissors();
    }

    public void drawPostProcessedItems(float delta) {
        updateScroll(delta);
        Pencil.I().beginPostProcessedOnlyDrawings();
        try {
            drawTabs(delta, true);
            Pencil.I().startScissors(GameContext.I().viewport.getCamera(),
                GameContext.I().batch.getTransformMatrix(), ITEMS_VIEWPORT);
            drawSelectedItems(delta, true);
            Pencil.I().addDrawing(new TextureDrawing(Assets.I().get(AssetKey.WHITE_PIXEL),
                ITEMS_VIEWPORT.x, ITEMS_VIEWPORT.y, 0.001f, 0.001f,
                ZIndex.SHOP_CARD_TOUCHING, new Color(1f, 1f, 1f, 0f)));
            Pencil.I().endScissors();
        } finally {
            Pencil.I().endPostProcessedOnlyDrawings();
        }
    }

    private void drawTabs(float delta, boolean inactiveOnly) {
        drawTabFor(Tab.SYMBOLS, SYMBOL_TAB_BOUNDS, symbolTabText, delta, inactiveOnly);
        drawTabFor(Tab.STATS, STATS_TAB_BOUNDS, statsTabText, delta, inactiveOnly);
        drawTabFor(Tab.UNLOCKS, UNLOCKS_TAB_BOUNDS, unlocksTabText, delta, inactiveOnly);
    }

    private void drawTabFor(Tab tab, Rectangle bounds, FabledText text,
                            float delta, boolean inactiveOnly) {
        boolean selected = selectedTab == tab;
        if (inactiveOnly && selected) return;
        drawTab(bounds, text, selected, selected ? delta : 0f);
    }

    private void drawSectionDivider() {
        Pencil.I().addDrawing(new TextureDrawing(Assets.I().get(AssetKey.BRIGHT_SLATE_PIXEL),
            GameplayLayout.HUD_LEFT, 6.75f + GameplayLayout.HUD_Y_OFFSET,
            GameplayLayout.HUD_WIDTH, 0.025f, ZIndex.SHOP_CARD,
            new Color(1f, 1f, 1f, 0.48f)));
    }

    private void drawTab(Rectangle bounds, FabledText text, boolean selected, float delta) {
        Pencil.I().addDrawing(new TextureDrawing(Assets.I().get(AssetKey.DARK_SLATE_PIXEL),
            bounds.x, bounds.y, bounds.width, bounds.height, ZIndex.SHOP_CARD,
            selected ? new Color(1f, 1f, 1f, 0.86f)
                : new Color(0.42f, 0.42f, 0.42f, 0.55f)));
        if (selected) Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.YELLOW_PIXEL), bounds.x, bounds.y,
            bounds.width, 0.04f, ZIndex.SHOP_CARD));
        text.setY(bounds.y + 0.15f);
        text.draw(delta);
    }

    private void drawSelectedItems(float delta, Boolean disabledFilter) {
        if (selectedTab == Tab.SYMBOLS) {
            drawSymbolItems(delta, disabledFilter);
        } else {
            drawUpgradeItems(delta, disabledFilter);
        }
    }

    private void drawUpgradeItems(float delta, Boolean disabledFilter) {
        float y = 4.86f + GameplayLayout.HUD_Y_OFFSET + selectedScrollOffset();
        for (ShopItem item : selectedItems()) {
            item.setHudRowBounds(new Rectangle(GameplayLayout.HUD_LEFT, y,
                GameplayLayout.HUD_WIDTH, UPGRADE_ROW_HEIGHT));
            drawItem(item, delta, disabledFilter);
            y -= UPGRADE_ROW_HEIGHT + UPGRADE_ROW_GAP;
        }
    }

    private void drawSymbolItems(float delta, Boolean disabledFilter) {
        float top = ITEMS_VIEWPORT.y + ITEMS_VIEWPORT.height
            - SYMBOL_ROW_HEIGHT + symbolScrollOffset;
        for (int index = 0; index < symbolItems.size(); index++) {
            float y = top - index * (SYMBOL_ROW_HEIGHT + SYMBOL_ROW_GAP);
            ShopItem item = symbolItems.get(index);
            item.setHudSymbolRowBounds(new Rectangle(GameplayLayout.HUD_LEFT,
                y, GameplayLayout.HUD_WIDTH, SYMBOL_ROW_HEIGHT));
            drawItem(item, delta, disabledFilter);
        }
    }

    private void drawItem(ShopItem item, float delta, Boolean disabledFilter) {
        boolean disabled = item.isDisabled();
        if (disabledFilter != null && disabled != disabledFilter) return;
        item.draw(disabledFilter == null && disabled ? 0f : delta);
    }

    private void updateScroll(float delta) {
        symbolScrollOffset = approachScroll(symbolScrollOffset, symbolScrollTarget, delta);
        statsScrollOffset = approachScroll(statsScrollOffset, statsScrollTarget, delta);
        unlocksScrollOffset = approachScroll(unlocksScrollOffset, unlocksScrollTarget, delta);
    }

    private float approachScroll(float current, float target, float delta) {
        float result = Interpolation.fade.apply(current, target, Math.min(1f, delta * 13f));
        return Math.abs(result - target) < 0.002f ? target : result;
    }

    private void drawScrollbar() {
        float maxScroll = maxSelectedScroll();
        if (maxScroll <= 0f) return;
        float trackX = ITEMS_VIEWPORT.x + ITEMS_VIEWPORT.width + 0.045f;
        float trackWidth = 0.045f;
        float thumbHeight = Math.max(0.55f,
            ITEMS_VIEWPORT.height * ITEMS_VIEWPORT.height / selectedContentHeight());
        float progress = selectedScrollOffset() / maxScroll;
        float thumbY = ITEMS_VIEWPORT.y + ITEMS_VIEWPORT.height - thumbHeight
            - progress * (ITEMS_VIEWPORT.height - thumbHeight);

        Pencil.I().addDrawing(new TextureDrawing(Assets.I().get(AssetKey.BLACK_PIXEL),
            trackX, ITEMS_VIEWPORT.y, trackWidth, ITEMS_VIEWPORT.height,
            ZIndex.SHOP_CARD, new Color(1f, 1f, 1f, 0.24f)));
        Pencil.I().addDrawing(new TextureDrawing(Assets.I().get(AssetKey.YELLOW_PIXEL),
            trackX - 0.015f, thumbY, trackWidth + 0.03f, thumbHeight, ZIndex.SHOP_CARD));
    }

    public void handleInput(Vector2 mouse, boolean pressed, boolean wasPressed) {
        handleTabInput(mouse, pressed, wasPressed);
        Vector2 itemMouse = ITEMS_VIEWPORT.contains(mouse) ? mouse : blockedMouse;
        for (ShopItem item : selectedItems()) {
            if (item.intersects(ITEMS_VIEWPORT)) {
                item.handleInput(itemMouse, pressed, wasPressed);
            }
        }
    }

    public boolean scroll(float amountY, Vector2 mouse) {
        if (amountY == 0f || !ITEMS_VIEWPORT.contains(mouse) || maxSelectedScroll() <= 0f) {
            return false;
        }
        float step = selectedTab == Tab.SYMBOLS
            ? SYMBOL_ROW_HEIGHT + SYMBOL_ROW_GAP
            : UPGRADE_ROW_HEIGHT + UPGRADE_ROW_GAP;
        setSelectedScrollTarget(MathUtils.clamp(
            selectedScrollTarget() + Math.signum(amountY) * step,
            0f, maxSelectedScroll()));
        return true;
    }

    public Rectangle getCollisionBounds() {
        return new Rectangle(COLLISION_BOUNDS);
    }

    private void handleTabInput(Vector2 mouse, boolean pressed, boolean wasPressed) {
        if (pressed && !wasPressed) {
            if (SYMBOL_TAB_BOUNDS.contains(mouse)) pressedTab = Tab.SYMBOLS;
            else if (STATS_TAB_BOUNDS.contains(mouse)) pressedTab = Tab.STATS;
            else if (UNLOCKS_TAB_BOUNDS.contains(mouse)) pressedTab = Tab.UNLOCKS;
            else pressedTab = null;
        } else if (!pressed && wasPressed) {
            if (pressedTab == Tab.SYMBOLS && SYMBOL_TAB_BOUNDS.contains(mouse)) {
                selectedTab = Tab.SYMBOLS;
            } else if (pressedTab == Tab.STATS && STATS_TAB_BOUNDS.contains(mouse)) {
                selectedTab = Tab.STATS;
            } else if (pressedTab == Tab.UNLOCKS && UNLOCKS_TAB_BOUNDS.contains(mouse)) {
                selectedTab = Tab.UNLOCKS;
            }
            pressedTab = null;
        }
    }

    private List<ShopItem> selectedItems() {
        if (selectedTab == Tab.STATS) return statsItems;
        if (selectedTab == Tab.UNLOCKS) return unlockItems;
        return symbolItems;
    }

    private float selectedContentHeight() {
        int rows = selectedItems().size();
        float height = selectedTab == Tab.SYMBOLS ? SYMBOL_ROW_HEIGHT : UPGRADE_ROW_HEIGHT;
        float gap = selectedTab == Tab.SYMBOLS ? SYMBOL_ROW_GAP : UPGRADE_ROW_GAP;
        return rows * height + Math.max(0, rows - 1) * gap;
    }

    private float maxSelectedScroll() {
        float padding = selectedTab == Tab.SYMBOLS ? 0f : 0.08f;
        return Math.max(0f, selectedContentHeight() - ITEMS_VIEWPORT.height + padding);
    }

    private float selectedScrollOffset() {
        if (selectedTab == Tab.STATS) return statsScrollOffset;
        if (selectedTab == Tab.UNLOCKS) return unlocksScrollOffset;
        return symbolScrollOffset;
    }

    private float selectedScrollTarget() {
        if (selectedTab == Tab.STATS) return statsScrollTarget;
        if (selectedTab == Tab.UNLOCKS) return unlocksScrollTarget;
        return symbolScrollTarget;
    }

    private void setSelectedScrollTarget(float target) {
        if (selectedTab == Tab.STATS) statsScrollTarget = target;
        else if (selectedTab == Tab.UNLOCKS) unlocksScrollTarget = target;
        else symbolScrollTarget = target;
    }

    private ShopItem symbolItem(Symbol symbol, int key) {
        return new ShopItem(cardTitle(symbol.toString()), symbol,
            Assets.I().get(symbol.textureKey()), key);
    }

    private static GeneratedFabledText text(String value, float fontSize,
                                             float spacing, float scale) {
        return new GeneratedFabledText(value, fontSize, spacing, scale,
            ZIndex.SHOP_CARD, true);
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
