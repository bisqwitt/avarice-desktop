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

/**
 * Between-round shop presented as a small persistent skill tree.
 *
 * Roots are always available. Buying a root once unlocks the next node in
 * that branch, while repeatable upgrades can still be bought on later visits.
 */
public final class Shop {

    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 9f;
    private static final float ENTER_DURATION = 0.24f;
    private static final float NODE_WIDTH = 2.34f;
    private static final float NODE_HEIGHT = 0.91f;

    private static final Color BACKDROP = new Color(0.012f, 0.021f, 0.029f, 1f);
    private static final Color PANEL = new Color(0.038f, 0.061f, 0.078f, 1f);
    private static final Color NODE = new Color(0.085f, 0.125f, 0.150f, 1f);
    private static final Color NODE_HOVER = new Color(0.13f, 0.19f, 0.22f, 1f);
    private static final Color NODE_LOCKED = new Color(0.045f, 0.065f, 0.078f, 1f);
    private static final Color GOLD = new Color(1f, 0.82f, 0.44f, 1f);
    private static final Color MUTED = new Color(0.54f, 0.62f, 0.67f, 1f);

    private static final Rectangle NEXT_ROUND_BOUNDS =
        new Rectangle(11.56f, 0.34f, 3.70f, 0.76f);

    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final GeneratedFabledText title = text("SKILL TREE", 16f, GOLD, true);
    private final GeneratedFabledText prompt = text(
        "INVEST YOUR CASH THEN START THE NEXT ROUND", 43f, MUTED, false
    );
    private final GeneratedFabledText balanceLabel = text("CASH", 39f, MUTED, true);
    private final GeneratedFabledText nextRoundText = text(
        "START NEXT ROUND", 39f, GOLD, true
    );
    private final GeneratedFabledText controlsText = text(
        "ENTER TO CONTINUE", 48f, MUTED, false
    );
    private final GeneratedFabledText fruitBranch = branchText("FRUIT");
    private final GeneratedFabledText luckyBranch = branchText("LUCKY");
    private final GeneratedFabledText metalBranch = branchText("METAL");
    private final GeneratedFabledText machineBranch = branchText("MACHINE");
    private final GeneratedFabledText utilityBranch = branchText("UTILITY");

    private final CreditNumber balance = new CreditNumber(
        ScoreDisplay.I().getScoreNumber(),
        new Rectangle(13.05f, 7.82f, 0.24f, 0.38f),
        0.32f
    ).setZIndex(ZIndex.SHOP_CARD);

    private final List<SkillNode> nodes = new ArrayList<>();
    private final Runnable onReturnedFromShop;

    private enum State { HIDDEN, ENTERING, SHOWN, EXITING }

    private State state = State.HIDDEN;
    private SkillNode pressedNode;
    private boolean nextRoundHovered;
    private boolean nextRoundPressed;
    private boolean inputArmed;
    private float transition;

    public Shop(Runnable onReturnedFromShop) {
        this.onReturnedFromShop = onReturnedFromShop;
        balance.setCompactThreshold(1_000f);
        balance.setDigitSpacing(0.20f);
        balance.getIdleScaleEffect().setAllowed(false);
        ScoreDisplay.I().addScoreChangeListener(
            event -> balance.setValue(((Number) event.getNewValue()).floatValue())
        );

        buildTree();
    }

    private void buildTree() {
        SkillNode lemon = symbolNode("LEMON", Symbol.LEMON, 2f, 3.22f, 6.32f, null);
        symbolNode("CHERRY", Symbol.CHERRY, 2f, 6.12f, 6.32f, lemon);

        SkillNode clover = symbolNode("CLOVER", Symbol.CLOVER, 3f, 3.22f, 5.15f, null);
        SkillNode bell = symbolNode("BELL", Symbol.BELL, 3f, 6.12f, 5.15f, clover);
        symbolNode("SEVEN", Symbol.SEVEN, 7f, 9.02f, 5.15f, bell);

        SkillNode iron = symbolNode("IRON", Symbol.IRON, 5f, 3.22f, 3.98f, null);
        symbolNode("DIAMOND", Symbol.DIAMOND, 5f, 6.12f, 3.98f, iron);

        Automations automations = Automations.I();
        SkillNode speed = upgradeNode(
            "SPEED", AssetKey.RETRIGGER, automations.getSlotMachineSpeed(),
            () -> automations.getSlotMachineSpeed().getSpeedPercent() != 100,
            automations.getSlotMachineSpeed()::isMaxSpeedReached,
            3.22f, 2.81f, null
        );
        SkillNode spinQueue = automationNode(
            "SPIN QUEUE", AssetKey.SPIN_BUTTON, automations.getSpinQueuer(),
            6.12f, 2.81f, speed
        );
        SkillNode queueSize = upgradeNode(
            "QUEUE LIMIT", AssetKey.SHOPPING_CART, automations.getAutoSpinCapacity(),
            () -> automations.getAutoSpinCapacity().getCapacity() > 3,
            () -> false,
            9.02f, 2.81f, spinQueue
        );
        automationNode(
            "AUTO SPIN", AssetKey.RETRIGGER, automations.getFullAutoSpin(),
            11.92f, 2.81f, queueSize
        );

        SkillNode collectors = upgradeNode(
            "COLLECTORS", AssetKey.COLLECTOR, automations.getCollectorCapacity(),
            () -> automations.getCollectorCapacity().getCount() > 0,
            () -> false,
            3.22f, 1.64f, null
        );
        upgradeNode(
            "PATTERNS", AssetKey.PLUS_SYMBOL, automations.getPatternUnlock(),
            () -> PatternUnlocks.I().getUnlockedCount() > 0,
            () -> PatternUnlocks.I().getLockedPatterns().isEmpty(),
            6.12f, 1.64f, collectors
        );
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
        pressedNode = null;
        nextRoundPressed = false;
        state = State.ENTERING;
        ButtonBoard.I().moveOut();
    }

    public void exit() {
        if (state == State.HIDDEN || state == State.EXITING) return;
        transition = 1f;
        state = State.EXITING;
        pressedNode = null;
        nextRoundPressed = false;
        ButtonBoard.I().moveIn();
    }

    public void draw(float delta) {
        if (state == State.HIDDEN) return;

        float offsetY = screenOffsetY();
        drawHeader(delta, offsetY);
        drawConnectors(offsetY);
        drawBranchLabels(delta, offsetY);
        for (SkillNode node : nodes) node.draw(delta, offsetY);
        drawNextRoundButton(delta, offsetY);
    }

    /** Draw the opaque shop surface as part of the CRT capture. */
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

        balanceLabel.setAbsoluteX(11.75f);
        balanceLabel.setY(7.96f + offsetY);
        balanceLabel.draw(delta);
        balance.getFirstDigitBounds().setY(7.72f + offsetY);
        balance.getFirstDigitBounds().setX(12.88f);
        balance.draw(delta);
    }

    private void drawBranchLabels(float delta, float offsetY) {
        drawBranchLabel(fruitBranch, 6.58f + offsetY, delta);
        drawBranchLabel(luckyBranch, 5.41f + offsetY, delta);
        drawBranchLabel(metalBranch, 4.24f + offsetY, delta);
        drawBranchLabel(machineBranch, 3.07f + offsetY, delta);
        drawBranchLabel(utilityBranch, 1.90f + offsetY, delta);
    }

    private void drawBranchLabel(GeneratedFabledText label, float y, float delta) {
        label.setAbsoluteX(0.88f);
        label.setY(y);
        label.draw(delta);
    }

    private void drawConnectors(float offsetY) {
        for (SkillNode node : nodes) {
            if (node.prerequisite == null) continue;
            Rectangle from = node.prerequisite.bounds;
            Rectangle to = node.bounds;
            float x = from.x + from.width;
            float y = from.y + from.height / 2f + offsetY;
            float width = Math.max(0f, to.x - x);
            Color color = node.unlocked() ? GOLD : MUTED;
            rect(x, y - 0.035f, width, 0.07f, color,
                node.unlocked() ? 0.70f : 0.24f, ZIndex.SHOP_CARD);
        }
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

        nextRoundHovered = NEXT_ROUND_BOUNDS.contains(mouse);
        for (SkillNode node : nodes) node.hovered = node.bounds.contains(mouse);

        boolean enterDown = Gdx.input.isKeyPressed(Input.Keys.ENTER);
        if (!inputArmed) {
            if (!pressed && !enterDown) inputArmed = true;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            exit();
            return;
        }

        if (pressed && !wasPressed) {
            pressedNode = hoveredNode();
            nextRoundPressed = nextRoundHovered;
        } else if (!pressed && wasPressed) {
            SkillNode releasedNode = hoveredNode();
            if (pressedNode != null && pressedNode == releasedNode) {
                pressedNode.tryPurchase();
            } else if (nextRoundPressed && nextRoundHovered) {
                exit();
            }
            pressedNode = null;
            nextRoundPressed = false;
        }
    }

    private SkillNode hoveredNode() {
        for (SkillNode node : nodes) {
            if (node.hovered) return node;
        }
        return null;
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

    public boolean scrollItems(float amountY) {
        return false;
    }

    /** Kept for callers compiled against the previous store. */
    public boolean scrollAutomations(float amountY) {
        return false;
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

    private final class SkillNode {
        private final Rectangle bounds;
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
            Rectangle bounds,
            SkillNode prerequisite
        ) {
            this.bounds = bounds;
            this.icon = icon;
            this.target = target;
            this.majorPurchase = majorPurchase;
            this.prerequisite = prerequisite;
            this.name = text(name, 32f, Color.WHITE, true);
            this.name.fitWithinWidth(bounds.width - 0.82f);
            price = new CreditNumber(
                target.price(),
                new Rectangle(bounds.x + 0.72f, bounds.y + 0.12f, 0.14f, 0.23f),
                0.19f
            ).setZIndex(ZIndex.SHOP_CARD);
            price.setCompactThreshold(1_000f);
            price.setDigitSpacing(0.13f);
            price.getIdleScaleEffect().setAllowed(false);
        }

        private boolean unlocked() {
            return prerequisite == null || prerequisite.invested();
        }

        private boolean invested() {
            return purchasedHere || target.invested();
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
            Rectangle moved = movedBounds(bounds, offsetY);
            boolean unlocked = unlocked();
            boolean complete = target.complete();
            boolean affordable = target.canBuy();
            Color fill = !unlocked
                ? NODE_LOCKED
                : hovered && state == State.SHOWN ? NODE_HOVER : NODE;

            rect(moved.x + 0.05f, moved.y - 0.06f, moved.width, moved.height,
                Color.BLACK, 0.48f, ZIndex.SHOP_CARD);
            rect(moved.x, moved.y, moved.width, moved.height,
                fill, unlocked ? 1f : 0.82f, ZIndex.SHOP_CARD);
            rect(moved.x, moved.y + moved.height - 0.045f, moved.width, 0.045f,
                unlocked ? GOLD : MUTED, unlocked ? 0.76f : 0.20f,
                ZIndex.SHOP_CARD);

            Color iconColor = unlocked
                ? Color.WHITE
                : new Color(0.38f, 0.42f, 0.44f, 0.62f);
            Pencil.I().addDrawing(new TextureDrawing(
                icon, moved.x + 0.16f, moved.y + 0.18f,
                0.54f, 0.54f, ZIndex.SHOP_CARD, iconColor
            ));

            name.setAbsoluteX(moved.x + 0.80f);
            name.setY(moved.y + 0.55f);
            name.setOpacity(unlocked ? 1f : 0.45f);
            name.draw(delta);

            if (!unlocked) {
                drawNodeStatus(lockedText, moved, delta);
            } else if (complete) {
                drawNodeStatus(majorPurchase ? ownedText : maxedText, moved, delta);
            } else {
                if (Float.compare(price.getValue(), target.price()) != 0) {
                    price.setValue(target.price());
                }
                price.getFirstDigitBounds().set(moved.x + 0.80f, moved.y + 0.13f,
                    0.14f, 0.23f);
                price.setColor(affordable ? Assets.I().yellow() : MUTED);
                price.draw(delta);
            }

            if (purchaseFlash > 0f) {
                float duration = majorPurchase ? 0.55f : 0.38f;
                float alpha = MathUtils.clamp(purchaseFlash / duration, 0f, 1f);
                rect(moved.x, moved.y, moved.width, moved.height,
                    GOLD, alpha * alpha * 0.24f, ZIndex.SHOP_CARD_TOUCHING);
            }
        }

        private void drawNodeStatus(
            GeneratedFabledText status,
            Rectangle moved,
            float delta
        ) {
            status.setAbsoluteX(moved.x + 0.80f);
            status.setY(moved.y + 0.15f);
            status.draw(delta);
        }
    }
}
