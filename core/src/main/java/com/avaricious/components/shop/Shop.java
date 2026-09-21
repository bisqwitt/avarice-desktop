package com.avaricious.components.shop;

import com.avaricious.CreditNumber;
import com.avaricious.DevTools;
import com.avaricious.audio.AudioManager;
import com.avaricious.components.ButtonBoard;
import com.avaricious.components.ScreenShake;
import com.avaricious.components.automations.AbstractAutomation;
import com.avaricious.components.automations.AbstractAutomationUpgrade;
import com.avaricious.components.automations.Automations;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.slot.pattern.PatternUnlocks;
import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/** A pannable, zoomable skill tree shown between successful rounds. */
public final class Shop {

    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;
    private static final float ENTER_DURATION = 0.24f;

    private static final float NODE_WIDTH = 2.34f;
    private static final float NODE_HEIGHT = 0.91f;
    private static final Rectangle HUB_BOUNDS =
        new Rectangle(-1.30f, -0.55f, 2.60f, 1.10f);
    private static final Rectangle TREE_VIEWPORT =
        new Rectangle(0.70f, 1.30f, 14.60f, 5.85f);
    private static final float CANVAS_CENTER_X = 8f;
    private static final float CANVAS_CENTER_Y = 4.225f;
    private static final float INITIAL_ZOOM = 0.82f;
    private static final float MIN_ZOOM = 0.42f;
    private static final float MAX_ZOOM = 1.65f;
    private static final float DRAG_THRESHOLD = 0.08f;
    private static final float BALANCE_RIGHT = 14.92f;

    private static final Rectangle NEXT_ROUND_BOUNDS =
        new Rectangle(11.56f, 0.34f, 3.70f, 0.76f);

    private static final Color BACKDROP = new Color(0.012f, 0.021f, 0.029f, 1f);
    private static final Color PANEL = new Color(0.038f, 0.061f, 0.078f, 1f);
    private static final Color CANVAS = new Color(0.021f, 0.037f, 0.049f, 1f);
    private static final Color NODE = new Color(0.085f, 0.125f, 0.150f, 1f);
    private static final Color NODE_HOVER = new Color(0.13f, 0.19f, 0.22f, 1f);
    private static final Color NODE_LOCKED = new Color(0.045f, 0.065f, 0.078f, 1f);
    private static final Color GOLD = new Color(1f, 0.82f, 0.44f, 1f);
    private static final Color MUTED = new Color(0.54f, 0.62f, 0.67f, 1f);

    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final TextureRegion spade = Assets.I().get(AssetKey.SPADE);

    private final GeneratedFabledText title = text("SKILL TREE", 16f, GOLD, true);
    private final GeneratedFabledText prompt = text(
        "BUILD YOUR PATH BETWEEN ROUNDS", 43f, MUTED, false
    );
    private final GeneratedFabledText nextRoundText = text(
        "START NEXT ROUND", 39f, GOLD, true
    );
    private final GeneratedFabledText controlsText = text(
        "DRAG TO MOVE   WHEEL TO SCALE   C TO CENTER", 48f, MUTED, false
    );
    private final GeneratedFabledText hubText = text("CORE", 27f, GOLD, true);
    private final GeneratedFabledText fruitBranch = branchText("FRUIT");
    private final GeneratedFabledText luckyBranch = branchText("LUCKY");
    private final GeneratedFabledText metalBranch = branchText("METAL");
    private final GeneratedFabledText machineBranch = branchText("MACHINE");
    private final GeneratedFabledText utilityBranch = branchText("UTILITY");

    private final CreditNumber balance = new CreditNumber(
        ScoreDisplay.I().getScoreNumber(),
        new Rectangle(0f, 0f, 0.32f, 0.50f),
        0.38f
    ).setZIndex(ZIndex.SHOP_CARD);

    private final List<SkillNode> nodes = new ArrayList<>();
    private final List<Branch> branches = new ArrayList<>();
    private final Vector2 pan = new Vector2();
    private final Vector2 lastMouse = new Vector2(CANVAS_CENTER_X, CANVAS_CENTER_Y);
    private final Vector2 dragStart = new Vector2();
    private final Vector2 lastDragMouse = new Vector2();
    private final Runnable onReturnedFromShop;

    private enum State { HIDDEN, ENTERING, SHOWN, EXITING }

    private State state = State.HIDDEN;
    private SkillNode pressedNode;
    private boolean nextRoundHovered;
    private boolean nextRoundPressed;
    private boolean dragCandidate;
    private boolean dragging;
    private boolean inputArmed;
    private float transition;
    private float zoom = INITIAL_ZOOM;

    public Shop(Runnable onReturnedFromShop) {
        this.onReturnedFromShop = onReturnedFromShop;
        balance.setCompactThreshold(1_000f);
        balance.getIdleScaleEffect().setAllowed(false);
        balance.getPulseEffect().setStrength(0.35f);
        balance.getPulseEffect().setSpeed(0.09f);
        ScoreDisplay.I().addScoreChangeListener(
            event -> balance.setValue(((Number) event.getNewValue()).floatValue())
        );
        buildTree();
    }

    private void buildTree() {
        SkillNode lemon = symbolNode("LEMON", Symbol.LEMON, 2f, -4.0f, 1.9f, null);
        symbolNode("CHERRY", Symbol.CHERRY, 2f, -7.2f, 3.8f, lemon);
        branches.add(new Branch(fruitBranch, lemon, -2.65f, 1.18f));

        SkillNode clover = symbolNode("CLOVER", Symbol.CLOVER, 3f, 2.4f, 2.2f, null);
        SkillNode bell = symbolNode("BELL", Symbol.BELL, 3f, 4.8f, 4.3f, clover);
        symbolNode("SEVEN", Symbol.SEVEN, 7f, 7.2f, 6.4f, bell);
        branches.add(new Branch(luckyBranch, clover, 1.30f, 1.48f));

        SkillNode iron = symbolNode("IRON", Symbol.IRON, 5f, 3.4f, -0.7f, null);
        symbolNode("DIAMOND", Symbol.DIAMOND, 5f, 6.8f, -1.6f, iron);
        branches.add(new Branch(metalBranch, iron, 1.65f, -0.18f));

        Automations automations = Automations.I();
        SkillNode speed = upgradeNode(
            "SPEED", AssetKey.RETRIGGER, automations.getSlotMachineSpeed(),
            () -> automations.getSlotMachineSpeed().getSpeedPercent() != 100,
            automations.getSlotMachineSpeed()::isMaxSpeedReached,
            -4.0f, -2.2f, null
        );
        SkillNode quickSpin = automationNode(
            "QUICK SPIN", AssetKey.SPIN_BUTTON, automations.getQuickSpin(),
            -6.8f, -3.4f, speed
        );
        SkillNode spinQueue = automationNode(
            "SPIN QUEUE", AssetKey.SPIN_BUTTON, automations.getSpinQueuer(),
            -9.6f, -4.5f, quickSpin
        );
        SkillNode queueLimit = upgradeNode(
            "QUEUE LIMIT", AssetKey.SHOPPING_CART, automations.getAutoSpinCapacity(),
            () -> automations.getAutoSpinCapacity().getCapacity() > 3,
            () -> false,
            -12.4f, -5.4f, spinQueue
        );
        automationNode(
            "AUTO SPIN", AssetKey.RETRIGGER, automations.getFullAutoSpin(),
            -15.2f, -6.3f, queueLimit
        );
        branches.add(new Branch(machineBranch, speed, -2.85f, -1.45f));

        SkillNode collectors = upgradeNode(
            "COLLECTORS", AssetKey.COLLECTOR, automations.getCollectorCapacity(),
            () -> automations.getCollectorCapacity().getCount() > 0,
            () -> false,
            -1.15f, -3.0f, null
        );
        upgradeNode(
            "PATTERNS", AssetKey.PLUS_SYMBOL, automations.getPatternUnlock(),
            () -> PatternUnlocks.I().getUnlockedCount() > 0,
            () -> PatternUnlocks.I().getLockedPatterns().isEmpty(),
            -1.0f, -5.9f, collectors
        );
        branches.add(new Branch(utilityBranch, collectors, -0.82f, -1.72f));
    }

    private SkillNode symbolNode(
        String name,
        Symbol symbol,
        float initialValue,
        float x,
        float y,
        SkillNode prerequisite
    ) {
        PurchaseTarget target = new PurchaseTarget() {
            @Override
            public float price() {
                return SymbolValues.I().getPrice(symbol);
            }

            @Override
            public boolean canBuy() {
                return DevTools.freeShopPurchases()
                    || ScoreDisplay.I().getScoreNumber() >= price();
            }

            @Override
            public void purchase() {
                SymbolValues.I().increaseValue(symbol);
            }

            @Override
            public boolean invested() {
                return SymbolValues.I().getValue(symbol) > initialValue;
            }

            @Override
            public boolean complete() {
                return false;
            }
        };
        return addNode(name, Assets.I().get(symbol.textureKey()), target,
            false, x, y, prerequisite);
    }

    private SkillNode automationNode(
        String name,
        AssetKey icon,
        AbstractAutomation automation,
        float x,
        float y,
        SkillNode prerequisite
    ) {
        PurchaseTarget target = new PurchaseTarget() {
            @Override
            public float price() {
                return automation.price();
            }

            @Override
            public boolean canBuy() {
                return automation.isBuyable();
            }

            @Override
            public void purchase() {
                automation.activate();
            }

            @Override
            public boolean invested() {
                return automation.isActive();
            }

            @Override
            public boolean complete() {
                return automation.isActive();
            }
        };
        return addNode(name, Assets.I().get(icon), target, true, x, y, prerequisite);
    }

    private SkillNode upgradeNode(
        String name,
        AssetKey icon,
        AbstractAutomationUpgrade upgrade,
        BooleanSupplier invested,
        BooleanSupplier complete,
        float x,
        float y,
        SkillNode prerequisite
    ) {
        PurchaseTarget target = new PurchaseTarget() {
            @Override
            public float price() {
                return upgrade.price();
            }

            @Override
            public boolean canBuy() {
                return upgrade.isBuyable();
            }

            @Override
            public void purchase() {
                upgrade.upgrade();
            }

            @Override
            public boolean invested() {
                return invested.getAsBoolean();
            }

            @Override
            public boolean complete() {
                return complete.getAsBoolean();
            }
        };
        return addNode(name, Assets.I().get(icon), target, true, x, y, prerequisite);
    }

    private SkillNode addNode(
        String name,
        TextureRegion icon,
        PurchaseTarget target,
        boolean majorPurchase,
        float x,
        float y,
        SkillNode prerequisite
    ) {
        SkillNode node = new SkillNode(
            name,
            icon,
            target,
            majorPurchase,
            new Rectangle(x, y, NODE_WIDTH, NODE_HEIGHT),
            prerequisite
        );
        nodes.add(node);
        return node;
    }

    public void show() {
        if (state != State.HIDDEN) return;
        transition = 0f;
        inputArmed = false;
        clearPointerState();
        state = State.ENTERING;
        ButtonBoard.I().moveOut();
    }

    public void exit() {
        if (state == State.HIDDEN || state == State.EXITING) return;
        transition = 1f;
        clearPointerState();
        state = State.EXITING;
        ButtonBoard.I().moveIn();
    }

    public void draw(float delta) {
        if (state == State.HIDDEN) return;

        float offsetY = screenOffsetY();
        drawCanvas(delta, offsetY);
        drawHeader(delta, offsetY);
        drawNextRoundButton(delta, offsetY);
    }

    /** Draw the shop surface as part of the CRT capture. */
    public void drawPostProcessedItems(float delta) {
        if (state == State.HIDDEN) return;
        update(delta);

        float eased = Interpolation.pow3Out.apply(transition);
        float offsetY = (1f - eased) * WORLD_HEIGHT;
        Pencil.I().beginPostProcessedOnlyDrawings();
        try {
            rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT, BACKDROP,
                0.96f * eased, ZIndex.SHOP);
            rect(0.55f, 0.24f + offsetY, 14.90f, 8.30f,
                PANEL, 1f, ZIndex.SHOP);
            rect(TREE_VIEWPORT.x, TREE_VIEWPORT.y + offsetY,
                TREE_VIEWPORT.width, TREE_VIEWPORT.height,
                CANVAS, 1f, ZIndex.SHOP);
            rect(0.55f, 8.49f + offsetY, 14.90f, 0.05f,
                GOLD, 0.78f, ZIndex.SHOP_CARD);
        } finally {
            Pencil.I().endPostProcessedOnlyDrawings();
        }
    }

    private void drawHeader(float delta, float offsetY) {
        title.setAbsoluteX(0.92f);
        title.setY(7.77f + offsetY);
        title.draw(delta);

        prompt.setAbsoluteX(0.94f);
        prompt.setY(7.30f + offsetY);
        prompt.draw(delta);

        balance.getFirstDigitBounds().set(
            BALANCE_RIGHT - balance.getWidth(),
            7.855f + offsetY,
            0.32f,
            0.50f
        );
        balance.draw(delta);
    }

    private void drawCanvas(float delta, float offsetY) {
        Rectangle viewport = movedBounds(TREE_VIEWPORT, offsetY);
        Pencil.I().startScissors(
            GameContext.I().viewport.getCamera(),
            GameContext.I().batch.getTransformMatrix(),
            viewport
        );
        drawCanvasGrid(offsetY);
        drawConnectors(offsetY);
        drawBranchLabels(delta, offsetY);
        drawHub(delta, offsetY);
        for (SkillNode node : nodes) node.draw(delta, offsetY);
        Pencil.I().endScissors();
    }

    private void drawCanvasGrid(float offsetY) {
        float spacing = 2.5f;
        for (float localX = -30f; localX <= 30f; localX += spacing) {
            Vector2 screen = canvasToScreen(localX, 0f, offsetY);
            if (screen.x < TREE_VIEWPORT.x || screen.x > TREE_VIEWPORT.x + TREE_VIEWPORT.width) {
                continue;
            }
            rect(screen.x - 0.008f, TREE_VIEWPORT.y + offsetY,
                0.016f, TREE_VIEWPORT.height, MUTED, 0.055f, ZIndex.SHOP);
        }
        for (float localY = -25f; localY <= 25f; localY += spacing) {
            Vector2 screen = canvasToScreen(0f, localY, offsetY);
            if (screen.y < TREE_VIEWPORT.y + offsetY
                || screen.y > TREE_VIEWPORT.y + TREE_VIEWPORT.height + offsetY) {
                continue;
            }
            rect(TREE_VIEWPORT.x, screen.y - 0.008f,
                TREE_VIEWPORT.width, 0.016f, MUTED, 0.055f, ZIndex.SHOP);
        }
    }

    private void drawConnectors(float offsetY) {
        Vector2 hubCenter = canvasToScreen(0f, 0f, offsetY);
        for (Branch branch : branches) {
            Vector2 rootCenter = branch.root.screenCenter(offsetY);
            drawLine(hubCenter, rootCenter, GOLD, 0.58f, offsetY);
        }

        for (SkillNode node : nodes) {
            if (node.prerequisite == null) continue;
            Color color = node.unlocked() ? GOLD : MUTED;
            drawLine(
                node.prerequisite.screenCenter(offsetY),
                node.screenCenter(offsetY),
                color,
                node.unlocked() ? 0.70f : 0.22f,
                offsetY
            );
        }
    }

    private void drawLine(
        Vector2 start,
        Vector2 end,
        Color color,
        float alpha,
        float offsetY
    ) {
        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
        float thickness = Math.max(0.025f, 0.065f * zoom);
        float midX = (start.x + end.x) / 2f;
        float midY = (start.y + end.y) / 2f;
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel,
            midX - length / 2f,
            midY - thickness / 2f,
            length,
            thickness,
            1f,
            angle,
            ZIndex.SHOP_CARD,
            new Color(color.r, color.g, color.b, alpha)
        ));
    }

    private void drawBranchLabels(float delta, float offsetY) {
        for (Branch branch : branches) {
            Vector2 position = canvasToScreen(branch.labelX, branch.labelY, offsetY);
            branch.label.setAnimationScale(zoom * 0.86f);
            branch.label.setAbsoluteX(position.x);
            branch.label.setY(position.y);
            branch.label.setOpacity(0.72f);
            branch.label.draw(delta);
        }
    }

    private void drawHub(float delta, float offsetY) {
        Rectangle bounds = canvasBoundsToScreen(HUB_BOUNDS, offsetY);
        rect(bounds.x + 0.07f * zoom, bounds.y - 0.08f * zoom,
            bounds.width, bounds.height, Color.BLACK, 0.58f, ZIndex.SHOP_CARD);
        rect(bounds.x, bounds.y, bounds.width, bounds.height,
            new Color(0.12f, 0.13f, 0.10f, 1f), 1f, ZIndex.SHOP_CARD);
        rect(bounds.x, bounds.y + bounds.height - 0.07f * zoom,
            bounds.width, 0.07f * zoom, GOLD, 1f, ZIndex.SHOP_CARD);

        float iconSize = 0.68f * zoom;
        Pencil.I().addDrawing(new TextureDrawing(
            spade,
            bounds.x + 0.20f * zoom,
            bounds.y + 0.19f * zoom,
            iconSize,
            iconSize,
            ZIndex.SHOP_CARD
        ));

        hubText.setAnimationScale(zoom);
        hubText.setAbsoluteX(bounds.x + 1.02f * zoom);
        hubText.setY(bounds.y + 0.40f * zoom);
        hubText.setOpacity(1f);
        hubText.draw(delta);
    }

    private void drawNextRoundButton(float delta, float offsetY) {
        Rectangle bounds = movedBounds(NEXT_ROUND_BOUNDS, offsetY);
        Color fill = nextRoundHovered && inputArmed ? NODE_HOVER : NODE;
        rect(bounds.x + 0.06f, bounds.y - 0.07f, bounds.width, bounds.height,
            Color.BLACK, 0.52f, ZIndex.SHOP_CARD);
        rect(bounds.x, bounds.y, bounds.width, bounds.height,
            fill, 1f, ZIndex.SHOP_CARD);
        rect(bounds.x, bounds.y, bounds.width, 0.05f,
            GOLD, 0.95f, ZIndex.SHOP_CARD);

        centerText(nextRoundText, bounds, 0.25f, delta);
        controlsText.setAbsoluteX(0.92f);
        controlsText.setY(0.59f + offsetY);
        controlsText.draw(delta);
    }

    public void handleInput(
        Vector2 mouse,
        boolean pressed,
        boolean wasPressed,
        float delta
    ) {
        if (state != State.SHOWN) return;

        lastMouse.set(mouse);
        boolean overCanvas = TREE_VIEWPORT.contains(mouse);
        nextRoundHovered = NEXT_ROUND_BOUNDS.contains(mouse);
        for (SkillNode node : nodes) {
            node.hovered = overCanvas && !dragging && node.contains(mouse);
        }

        boolean enterDown = Gdx.input.isKeyPressed(Input.Keys.ENTER);
        if (!inputArmed) {
            if (!pressed && !enterDown) inputArmed = true;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            exit();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            resetView();
            AudioManager.I().playHover();
        }

        if (pressed && !wasPressed) {
            nextRoundPressed = nextRoundHovered;
            if (!nextRoundPressed && overCanvas) {
                dragCandidate = true;
                dragging = false;
                dragStart.set(mouse);
                lastDragMouse.set(mouse);
                pressedNode = hoveredNode();
            }
            return;
        }

        if (pressed && wasPressed && dragCandidate) {
            if (!dragging && dragStart.dst2(mouse) >= DRAG_THRESHOLD * DRAG_THRESHOLD) {
                dragging = true;
                pressedNode = null;
            }
            if (dragging) {
                pan.add(mouse.x - lastDragMouse.x, mouse.y - lastDragMouse.y);
            }
            lastDragMouse.set(mouse);
            return;
        }

        if (!pressed && wasPressed) {
            SkillNode releasedNode = hoveredNode();
            if (!dragging && pressedNode != null && pressedNode == releasedNode) {
                pressedNode.tryPurchase();
            } else if (nextRoundPressed && nextRoundHovered) {
                exit();
            }
            clearPointerState();
        }
    }

    private SkillNode hoveredNode() {
        for (SkillNode node : nodes) {
            if (node.hovered) return node;
        }
        return null;
    }

    public boolean scrollItems(float amountY, Vector2 anchor) {
        if (state != State.SHOWN || amountY == 0f) return false;
        Vector2 zoomAnchor = anchor == null ? lastMouse : anchor;
        if (!TREE_VIEWPORT.contains(zoomAnchor)) return false;

        float oldZoom = zoom;
        float newZoom = MathUtils.clamp(
            oldZoom * (float) Math.pow(0.86f, amountY),
            MIN_ZOOM,
            MAX_ZOOM
        );
        if (Float.compare(oldZoom, newZoom) == 0) return true;

        float ratio = newZoom / oldZoom;
        float relativeX = zoomAnchor.x - CANVAS_CENTER_X - pan.x;
        float relativeY = zoomAnchor.y - CANVAS_CENTER_Y - pan.y;
        pan.x += relativeX * (1f - ratio);
        pan.y += relativeY * (1f - ratio);
        zoom = newZoom;
        return true;
    }

    public boolean scrollItems(float amountY) {
        return scrollItems(amountY, lastMouse);
    }

    /** Kept for callers compiled against the previous store. */
    public boolean scrollAutomations(float amountY) {
        return scrollItems(amountY);
    }

    private void resetView() {
        pan.setZero();
        zoom = INITIAL_ZOOM;
    }

    private void clearPointerState() {
        pressedNode = null;
        nextRoundPressed = false;
        dragCandidate = false;
        dragging = false;
    }

    private void update(float delta) {
        for (SkillNode node : nodes) node.update(delta);
        float step = delta / ENTER_DURATION;
        if (state == State.ENTERING) {
            transition = Math.min(1f, transition + step);
            if (transition >= 1f) {
                state = State.SHOWN;
                ScreenShake.I().addTrauma(0.06f);
            }
        } else if (state == State.EXITING) {
            transition = Math.max(0f, transition - step);
            if (transition <= 0f) {
                state = State.HIDDEN;
                onReturnedFromShop.run();
            }
        }
    }

    private float screenOffsetY() {
        return (1f - Interpolation.pow3Out.apply(transition)) * WORLD_HEIGHT;
    }

    public boolean isShowing() {
        return state != State.HIDDEN;
    }

    private void purchase(PurchaseTarget target, boolean major) {
        if (!DevTools.freeShopPurchases()) {
            ScoreDisplay.I().removeFromScore(target.price());
        }
        target.purchase();
        AudioManager.I().playShopPurchase(major);
        ScreenShake.I().addTrauma(major ? 0.15f : 0.08f);
    }

    private Vector2 canvasToScreen(float x, float y, float offsetY) {
        return new Vector2(
            CANVAS_CENTER_X + pan.x + x * zoom,
            CANVAS_CENTER_Y + pan.y + y * zoom + offsetY
        );
    }

    private Rectangle canvasBoundsToScreen(Rectangle bounds, float offsetY) {
        Vector2 position = canvasToScreen(bounds.x, bounds.y, offsetY);
        return new Rectangle(
            position.x,
            position.y,
            bounds.width * zoom,
            bounds.height * zoom
        );
    }

    private static GeneratedFabledText text(
        String value,
        float size,
        Color color,
        boolean bigFirstLetter
    ) {
        GeneratedFabledText result = new GeneratedFabledText(
            value, size, 0.025f, 0.14f, ZIndex.SHOP_CARD, bigFirstLetter
        );
        result.setFloatEffects(0f, 0f);
        result.getWords().forEach(word -> word.setColor(color));
        return result;
    }

    private static GeneratedFabledText branchText(String value) {
        GeneratedFabledText result = text(value, 31f, MUTED, true);
        result.fitWithinWidth(1.72f);
        return result;
    }

    private void centerText(
        GeneratedFabledText text,
        Rectangle bounds,
        float yInset,
        float delta
    ) {
        text.setAbsoluteX(bounds.x + (bounds.width - text.getRenderedWidth()) / 2f);
        text.setY(bounds.y + yInset);
        text.draw(delta);
    }

    private static Rectangle movedBounds(Rectangle source, float offsetY) {
        return new Rectangle(source.x, source.y + offsetY, source.width, source.height);
    }

    private void rect(
        float x,
        float y,
        float width,
        float height,
        Color color,
        float alpha,
        ZIndex layer
    ) {
        Pencil.I().addDrawing(new TextureDrawing(
            whitePixel, x, y, width, height, layer,
            new Color(color.r, color.g, color.b, alpha)
        ));
    }

    private interface PurchaseTarget {
        float price();
        boolean canBuy();
        void purchase();
        boolean invested();
        boolean complete();
    }

    private static final class Branch {
        private final GeneratedFabledText label;
        private final SkillNode root;
        private final float labelX;
        private final float labelY;

        private Branch(
            GeneratedFabledText label,
            SkillNode root,
            float labelX,
            float labelY
        ) {
            this.label = label;
            this.root = root;
            this.labelX = labelX;
            this.labelY = labelY;
        }
    }

    private final class SkillNode {
        private static final float PRICE_DIGIT_WIDTH = 0.14f;
        private static final float PRICE_DIGIT_HEIGHT = 0.23f;
        private static final float PRICE_DIGIT_SPACING = 0.13f;

        private final Rectangle canvasBounds;
        private final TextureRegion icon;
        private final PurchaseTarget target;
        private final boolean majorPurchase;
        private final SkillNode prerequisite;
        private final GeneratedFabledText name;
        private final GeneratedFabledText lockedText = text("LOCKED", 43f, MUTED, false);
        private final GeneratedFabledText ownedText = text("OWNED", 43f, GOLD, false);
        private final GeneratedFabledText maxedText = text("MAXED", 43f, GOLD, false);
        private final CreditNumber price;

        private boolean hovered;
        private boolean purchasedHere;
        private float purchaseFlash;

        private SkillNode(
            String name,
            TextureRegion icon,
            PurchaseTarget target,
            boolean majorPurchase,
            Rectangle canvasBounds,
            SkillNode prerequisite
        ) {
            this.canvasBounds = canvasBounds;
            this.icon = icon;
            this.target = target;
            this.majorPurchase = majorPurchase;
            this.prerequisite = prerequisite;
            this.name = text(name, 32f, Color.WHITE, true);
            this.name.fitWithinWidth(canvasBounds.width - 0.82f);
            price = new CreditNumber(
                target.price(),
                new Rectangle(0f, 0f, PRICE_DIGIT_WIDTH, PRICE_DIGIT_HEIGHT),
                PRICE_DIGIT_SPACING
            ).setZIndex(ZIndex.SHOP_CARD);
            price.setCompactThreshold(1_000f);
            price.getIdleScaleEffect().setAllowed(false);
        }

        private boolean unlocked() {
            return prerequisite == null || prerequisite.invested();
        }

        private boolean invested() {
            return purchasedHere || target.invested();
        }

        private Vector2 screenCenter(float offsetY) {
            return canvasToScreen(
                canvasBounds.x + canvasBounds.width / 2f,
                canvasBounds.y + canvasBounds.height / 2f,
                offsetY
            );
        }

        private boolean contains(Vector2 mouse) {
            return canvasBoundsToScreen(canvasBounds, 0f).contains(mouse);
        }

        private void tryPurchase() {
            if (!unlocked() || target.complete() || !target.canBuy()) {
                AudioManager.I().playMiss();
                return;
            }
            purchase(target, majorPurchase);
            purchasedHere = true;
            purchaseFlash = majorPurchase ? 0.55f : 0.38f;
        }

        private void update(float delta) {
            purchaseFlash = Math.max(0f, purchaseFlash - delta);
        }

        private void draw(float delta, float offsetY) {
            Rectangle bounds = canvasBoundsToScreen(canvasBounds, offsetY);
            boolean unlocked = unlocked();
            boolean complete = target.complete();
            boolean affordable = target.canBuy();
            Color fill = !unlocked
                ? NODE_LOCKED
                : hovered && state == State.SHOWN ? NODE_HOVER : NODE;

            rect(bounds.x + 0.05f * zoom, bounds.y - 0.06f * zoom,
                bounds.width, bounds.height, Color.BLACK, 0.48f, ZIndex.SHOP_CARD);
            rect(bounds.x, bounds.y, bounds.width, bounds.height,
                fill, unlocked ? 1f : 0.82f, ZIndex.SHOP_CARD);
            rect(bounds.x, bounds.y + bounds.height - 0.045f * zoom,
                bounds.width, 0.045f * zoom,
                unlocked ? GOLD : MUTED, unlocked ? 0.76f : 0.20f,
                ZIndex.SHOP_CARD);

            Color iconColor = unlocked
                ? Color.WHITE
                : new Color(0.38f, 0.42f, 0.44f, 0.62f);
            Pencil.I().addDrawing(new TextureDrawing(
                icon,
                bounds.x + 0.16f * zoom,
                bounds.y + 0.18f * zoom,
                0.54f * zoom,
                0.54f * zoom,
                ZIndex.SHOP_CARD,
                iconColor
            ));

            name.setAnimationScale(zoom);
            name.setAbsoluteX(bounds.x + 0.80f * zoom);
            name.setY(bounds.y + 0.55f * zoom);
            name.setOpacity(unlocked ? 1f : 0.45f);
            name.draw(delta);

            if (!unlocked) {
                drawNodeStatus(lockedText, bounds, delta);
            } else if (complete) {
                drawNodeStatus(majorPurchase ? ownedText : maxedText, bounds, delta);
            } else {
                if (Float.compare(price.getValue(), target.price()) != 0) {
                    price.setValue(target.price());
                }
                price.setDigitSpacing(PRICE_DIGIT_SPACING * zoom);
                price.getFirstDigitBounds().set(
                    bounds.x + 0.80f * zoom,
                    bounds.y + 0.13f * zoom,
                    PRICE_DIGIT_WIDTH * zoom,
                    PRICE_DIGIT_HEIGHT * zoom
                );
                price.setColor(affordable ? Assets.I().yellow() : MUTED);
                price.draw(delta);
            }

            if (purchaseFlash > 0f) {
                float duration = majorPurchase ? 0.55f : 0.38f;
                float alpha = MathUtils.clamp(purchaseFlash / duration, 0f, 1f);
                rect(bounds.x, bounds.y, bounds.width, bounds.height,
                    GOLD, alpha * alpha * 0.24f, ZIndex.SHOP_CARD_TOUCHING);
            }
        }

        private void drawNodeStatus(
            GeneratedFabledText status,
            Rectangle bounds,
            float delta
        ) {
            status.setAnimationScale(zoom);
            status.setAbsoluteX(bounds.x + 0.80f * zoom);
            status.setY(bounds.y + 0.15f * zoom);
            status.draw(delta);
        }
    }
}
