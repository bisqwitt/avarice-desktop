package com.avaricious.components;

import com.avaricious.CreditNumber;
import com.avaricious.audio.AudioManager;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.slot.SlotMachine;
import com.avaricious.effects.particle.ParticleManager;
import com.avaricious.effects.particle.ParticleType;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.EconomyScaling;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/** Loose multiplier tickets: click one to buy it, then click it again to load it. */
public class TicketPressSystem {
    private static final Rectangle SELECTED_TICKET =
        new Rectangle(8.12f, 0.82f, 1.48f, 0.62f);
    private static final Rectangle PREVIOUS_TIER =
        new Rectangle(7.55f, 0.94f, 0.34f, 0.34f);
    private static final Rectangle NEXT_TIER =
        new Rectangle(9.83f, 0.94f, 0.34f, 0.34f);

    private static final float STACK_X = 10.18f;
    private static final float STACK_Y = 0.56f;
    private static final float STACK_OFFSET_X = 0.66f;
    private static final float STACK_OFFSET_Y = 0.16f;
    private static final float STACK_TICKET_WIDTH = 1.15f;
    private static final float STACK_TICKET_HEIGHT = 0.48f;
    private static final int QUEUE_CAPACITY = 3;
    private static final float PURCHASE_DURATION = 0.28f;
    private static final float FEED_DURATION = 0.34f;

    private static final Color DISABLED_COLOR =
        new Color(0.35f, 0.35f, 0.37f, 1f);
    private static final Color TICKET_INK =
        new Color(0.09f, 0.10f, 0.12f, 1f);
    private static final Color LIGHT_TICKET_INK =
        new Color(0.98f, 0.96f, 0.88f, 1f);

    private static final List<TicketTier> TIERS = Arrays.asList(
        new TicketTier(2, 1, 100f),
        new TicketTier(3, 1, 350f),
        new TicketTier(4, 3, 1_200f),
        new TicketTier(5, 5, 4_000f),
        new TicketTier(6, 7, 15_000f),
        new TicketTier(7, 8, 50_000f),
        new TicketTier(8, 9, 180_000f),
        new TicketTier(9, 10, 650_000f),
        new TicketTier(10, 12, 2_500_000f)
    );

    private static TicketPressSystem instance;

    public static TicketPressSystem I() {
        return instance == null ? instance = new TicketPressSystem() : instance;
    }

    private final List<Integer> ticketRack = new ArrayList<>();
    private final CreditNumber priceDisplay = new CreditNumber(
        40, new Rectangle(8.45f, 0.37f, 7 / 29f, 11 / 29f), 0.27f)
        .setZIndex(ZIndex.BUTTON_BOARD);

    private Supplier<Rectangle> spinBoundsSupplier;
    private int selectedTierIndex;
    private float displayedPrice = -1f;
    private int loadedMultiplier = 1;
    private int activeMultiplier = 1;
    private Integer purchasingMultiplier;
    private Integer feedingMultiplier;
    private float purchaseTimer;
    private float feedTimer;
    private float feedStartX;
    private float feedStartY;
    private float animationTime;
    private int pressedControl;
    private int pressedRackIndex = -1;
    private int hoveredRackIndex = -1;
    private boolean selectedTicketHovered;
    private boolean visible;

    private TicketPressSystem() {
        updatePriceDisplay();
    }

    public void init(Supplier<Rectangle> spinBoundsSupplier) {
        this.spinBoundsSupplier = spinBoundsSupplier;
    }

    public void reset() {
        ticketRack.clear();
        selectedTierIndex = 0;
        displayedPrice = -1f;
        loadedMultiplier = 1;
        activeMultiplier = 1;
        purchasingMultiplier = null;
        feedingMultiplier = null;
        purchaseTimer = 0f;
        feedTimer = 0f;
        pressedControl = 0;
        pressedRackIndex = -1;
        hoveredRackIndex = -1;
        selectedTicketHovered = false;
        updatePriceDisplay();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void update(float delta) {
        animationTime += delta;
        clampSelectedTier();
        updatePriceDisplay();

        if (purchasingMultiplier != null) {
            purchaseTimer += delta;
            if (purchaseTimer >= PURCHASE_DURATION) {
                purchasingMultiplier = null;
                purchaseTimer = 0f;
            }
        }

        if (feedingMultiplier == null) return;
        feedTimer += delta;
        if (feedTimer >= FEED_DURATION) finishLoadingTicket();
    }

    private void finishLoadingTicket() {
        loadedMultiplier = feedingMultiplier;
        feedingMultiplier = null;
        feedTimer = 0f;
        AudioManager.I().playTicketLoaded(loadedMultiplier);
        ScreenShake.I().addTrauma(0.06f + loadedMultiplier * 0.012f);

        Rectangle spin = spinBounds();
        if (spin == null) return;
        ParticleManager.I().create(
            spin.x + spin.width / 2f - SlotMachine.CELL_W / 2f,
            spin.y + spin.height / 2f - SlotMachine.CELL_H / 2f,
            ParticleType.RAINBOW, 0.015f, 24f + loadedMultiplier * 2f,
            ZIndex.BUTTON_BOARD);
    }

    public void draw(float delta) {
        if (!visible) return;

        drawTierSelector();
        priceDisplay.draw(delta);
        drawTicketStack();
        drawPurchaseAnimation();
        drawFeedingTicket();
        drawLoadedTicket();
        drawActiveMultiplier();
    }

    private void drawTierSelector() {
        drawArrow(PREVIOUS_TIER, true, selectedTierIndex > 0);
        drawArrow(NEXT_TIER, false, selectedTierIndex < unlockedTierCount() - 1);

        boolean affordable = canBuySelectedTicket();
        float scale = 1f;
        float lift = 0f;
        if (pressedControl == 2) {
            scale = 0.95f;
            lift = -0.015f;
        } else if (selectedTicketHovered && affordable) {
            scale = 1.07f;
            lift = 0.045f;
        }

        drawTicket(scaledBounds(SELECTED_TICKET, scale, lift),
            selectedTier().multiplier, affordable ? 1f : 0.52f,
            ZIndex.HAND_UI_CARD_DRAGGING);
    }

    private void drawArrow(Rectangle bounds, boolean left, boolean enabled) {
        Color color = enabled ? Assets.I().yellow() : DISABLED_COLOR;
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.ARROW_LETTER_SHADOW),
            bounds.x, bounds.y - 0.05f, bounds.width, bounds.height,
            1f, left ? 180f : 0f, ZIndex.BUTTON_BOARD,
            Assets.I().shadowColor()));
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.ARROW_LETTER),
            bounds.x, bounds.y, bounds.width, bounds.height,
            1f, left ? 180f : 0f, ZIndex.HAND_UI_CARD_DRAGGING, color));
    }

    private void drawTicketStack() {
        for (int index = 0; index < ticketRack.size(); index++) {
            if (purchasingMultiplier != null && index == ticketRack.size() - 1) continue;

            Rectangle bounds = stackTicketBounds(index);
            boolean canLoad = loadedMultiplier == 1 && feedingMultiplier == null
                && purchasingMultiplier == null;
            if (index == hoveredRackIndex && canLoad) {
                bounds = scaledBounds(bounds, 1.08f, 0.05f);
            }
            drawTicket(bounds, ticketRack.get(index), canLoad ? 1f : 0.74f,
                ZIndex.HAND_UI_CARD_DRAGGING);
        }
    }

    private Rectangle stackTicketBounds(int index) {
        return new Rectangle(
            STACK_X + index * STACK_OFFSET_X,
            STACK_Y + index * STACK_OFFSET_Y,
            STACK_TICKET_WIDTH,
            STACK_TICKET_HEIGHT);
    }

    private void drawPurchaseAnimation() {
        if (purchasingMultiplier == null || ticketRack.isEmpty()) return;

        float progress = Interpolation.pow2Out.apply(
            MathUtils.clamp(purchaseTimer / PURCHASE_DURATION, 0f, 1f));
        Rectangle target = stackTicketBounds(ticketRack.size() - 1);
        float arc = MathUtils.sin(progress * MathUtils.PI) * 0.25f;
        Rectangle moving = new Rectangle(
            MathUtils.lerp(SELECTED_TICKET.x, target.x, progress),
            MathUtils.lerp(SELECTED_TICKET.y, target.y, progress) + arc,
            MathUtils.lerp(SELECTED_TICKET.width, target.width, progress),
            MathUtils.lerp(SELECTED_TICKET.height, target.height, progress));
        drawTicket(moving, purchasingMultiplier, 1f,
            ZIndex.HAND_UI_CARD_DRAGGING);
    }

    private void drawFeedingTicket() {
        if (feedingMultiplier == null) return;
        Rectangle spin = spinBounds();
        if (spin == null) return;

        float progress = Interpolation.pow2Out.apply(
            MathUtils.clamp(feedTimer / FEED_DURATION, 0f, 1f));
        float endX = spin.x + spin.width * 0.50f - 0.46f;
        float endY = spin.y + spin.height * 0.50f - 0.18f;
        float arc = MathUtils.sin(progress * MathUtils.PI) * 0.30f;
        drawTicket(new Rectangle(
                MathUtils.lerp(feedStartX, endX, progress),
                MathUtils.lerp(feedStartY, endY, progress) + arc,
                MathUtils.lerp(STACK_TICKET_WIDTH, 0.92f, progress),
                MathUtils.lerp(STACK_TICKET_HEIGHT, 0.38f, progress)),
            feedingMultiplier, 1f, ZIndex.HAND_UI_CARD_DRAGGING);
    }

    private void drawLoadedTicket() {
        if (loadedMultiplier <= 1) return;
        Rectangle spin = spinBounds();
        if (spin == null) return;

        float bob = MathUtils.sin(animationTime * 5f) * 0.022f;
        drawTicket(new Rectangle(
                spin.x + spin.width - 0.64f,
                spin.y + spin.height - 0.04f + bob,
                0.70f, 0.31f),
            loadedMultiplier, 1f, ZIndex.HAND_UI_CARD_DRAGGING);
    }

    private void drawActiveMultiplier() {
        if (activeMultiplier <= 1) return;
        Rectangle spin = spinBounds();
        if (spin == null) return;

        float pulse = 1f + MathUtils.sin(animationTime * 9f) * 0.08f;
        drawMultiplier(
            spin.x + spin.width / 2f - 0.32f,
            spin.y + spin.height + 0.28f,
            0.64f, 0.35f, activeMultiplier,
            ZIndex.HAND_UI_CARD_DRAGGING, pulse, Assets.I().yellow());
    }

    private void drawTicket(Rectangle bounds, int multiplier, float alpha, ZIndex layer) {
        Color ticketColor = colorForMultiplier(multiplier, alpha);
        Color inkColor = inkForMultiplier(multiplier, alpha);
        float x = bounds.x;
        float y = bounds.y;
        float width = bounds.width;
        float height = bounds.height;

        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.BLACK_PIXEL),
            x + 0.04f, y - 0.055f, width, height,
            layer, Assets.I().shadowColor()));
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.WHITE_PIXEL),
            x, y, width, height, layer, ticketColor));

        Color highlight = new Color(
            Math.min(1f, ticketColor.r + 0.16f),
            Math.min(1f, ticketColor.g + 0.16f),
            Math.min(1f, ticketColor.b + 0.16f), alpha);
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.WHITE_PIXEL),
            x, y + height * 0.86f, width, height * 0.14f,
            layer, highlight));

        // Pixel cut-outs and perforations keep these reading as physical tickets.
        float notchWidth = width * 0.075f;
        float notchHeight = height * 0.24f;
        float notchY = y + (height - notchHeight) / 2f;
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.BLACK_PIXEL),
            x - 0.01f, notchY, notchWidth, notchHeight,
            layer, Color.BLACK));
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.BLACK_PIXEL),
            x + width - notchWidth + 0.01f, notchY, notchWidth, notchHeight,
            layer, Color.BLACK));

        for (int dash = 0; dash < 3; dash++) {
            float dashY = y + height * (0.18f + dash * 0.27f);
            drawPerforation(x + width * 0.18f, dashY, width, height, inkColor, layer);
            drawPerforation(x + width * 0.81f, dashY, width, height, inkColor, layer);
        }

        drawMultiplier(
            x + width * 0.29f, y + height * 0.19f,
            width * 0.43f, height * 0.62f,
            multiplier, layer, 1f, inkColor);
    }

    private void drawPerforation(
        float x, float y, float ticketWidth, float ticketHeight,
        Color color, ZIndex layer
    ) {
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.WHITE_PIXEL),
            x, y, ticketWidth * 0.025f, ticketHeight * 0.09f,
            layer, color));
    }

    private Color colorForMultiplier(int multiplier, float alpha) {
        switch (multiplier) {
            case 2: return new Color(1f, 0.76f, 0.20f, alpha);
            case 3: return new Color(0.64f, 0.42f, 1f, alpha);
            case 4: return new Color(0.30f, 0.68f, 1f, alpha);
            case 5: return new Color(0.32f, 0.86f, 0.55f, alpha);
            case 6: return new Color(0.30f, 0.90f, 0.76f, alpha);
            case 7: return new Color(1f, 0.43f, 0.20f, alpha);
            case 8: return new Color(1f, 0.30f, 0.34f, alpha);
            case 9: return new Color(0.96f, 0.30f, 0.76f, alpha);
            default: return new Color(1f, 0.24f, 0.62f, alpha);
        }
    }

    private Color inkForMultiplier(int multiplier, float alpha) {
        Color base = multiplier == 3 || multiplier == 8 || multiplier == 9
            ? LIGHT_TICKET_INK : TICKET_INK;
        return new Color(base.r, base.g, base.b, alpha);
    }

    private void drawMultiplier(
        float x, float y, float width, float height,
        int multiplier, ZIndex layer, float scale, Color color
    ) {
        String digits = String.valueOf(multiplier);
        float symbolWidth = width * 0.34f;
        float digitArea = width - symbolWidth;
        float digitWidth = digitArea / digits.length();
        Pencil.I().addDrawing(new TextureDrawing(
            Assets.I().get(AssetKey.MULT_SYMBOL),
            x, y, symbolWidth, height, scale, 0f, layer, color));
        for (int index = 0; index < digits.length(); index++) {
            int digit = Character.getNumericValue(digits.charAt(index));
            Pencil.I().addDrawing(new TextureDrawing(
                Assets.I().getDigitalNumber(digit),
                x + symbolWidth + index * digitWidth, y,
                digitWidth, height, scale, 0f, layer, color));
        }
    }

    public void handleInput(Vector2 mouse, boolean pressed, boolean wasPressed) {
        if (!visible) return;

        selectedTicketHovered = SELECTED_TICKET.contains(mouse);
        hoveredRackIndex = rackIndexAt(mouse);

        if (pressed && !wasPressed) {
            if (PREVIOUS_TIER.contains(mouse) && selectedTierIndex > 0) {
                pressedControl = -1;
            } else if (NEXT_TIER.contains(mouse)
                && selectedTierIndex < unlockedTierCount() - 1) {
                pressedControl = 1;
            } else if (SELECTED_TICKET.contains(mouse)) {
                pressedControl = 2;
            } else {
                pressedRackIndex = rackIndexAt(mouse);
            }
        }

        if (!pressed && wasPressed) {
            if (pressedControl == -1 && PREVIOUS_TIER.contains(mouse)) {
                selectPreviousTier();
            } else if (pressedControl == 1 && NEXT_TIER.contains(mouse)) {
                selectNextTier();
            } else if (pressedControl == 2 && SELECTED_TICKET.contains(mouse)) {
                tryBuySelectedTicket();
            } else if (pressedRackIndex >= 0
                && pressedRackIndex == rackIndexAt(mouse)) {
                loadTicket(pressedRackIndex);
            }
            pressedControl = 0;
            pressedRackIndex = -1;
        }

    }

    private int rackIndexAt(Vector2 mouse) {
        for (int index = ticketRack.size() - 1; index >= 0; index--) {
            if (stackTicketBounds(index).contains(mouse)) return index;
        }
        return -1;
    }

    private void selectPreviousTier() {
        if (selectedTierIndex <= 0) return;
        selectedTierIndex--;
        displayedPrice = -1f;
        AudioManager.I().playHover();
    }

    private void selectNextTier() {
        if (selectedTierIndex >= unlockedTierCount() - 1) return;
        selectedTierIndex++;
        displayedPrice = -1f;
        AudioManager.I().playHover();
    }

    private void tryBuySelectedTicket() {
        if (!canBuySelectedTicket()) {
            AudioManager.I().playMiss();
            return;
        }
        buySelectedTicket();
    }

    private void buySelectedTicket() {
        TicketTier tier = selectedTier();
        ScoreDisplay.I().removeFromScore(price(tier));
        ticketRack.add(tier.multiplier);
        purchasingMultiplier = tier.multiplier;
        purchaseTimer = 0f;
        AudioManager.I().playShopPurchase(false);
        ScreenShake.I().addTrauma(0.04f + tier.multiplier * 0.006f);
    }

    private boolean canBuySelectedTicket() {
        TicketTier tier = selectedTier();
        return purchasingMultiplier == null
            && ticketRack.size() < QUEUE_CAPACITY
            && ScoreDisplay.I().getScoreNumber() >= price(tier);
    }

    private void loadTicket(int rackIndex) {
        if (rackIndex < 0 || rackIndex >= ticketRack.size()
            || loadedMultiplier > 1 || feedingMultiplier != null
            || purchasingMultiplier != null) return;

        Rectangle source = stackTicketBounds(rackIndex);
        feedStartX = source.x;
        feedStartY = source.y;
        feedingMultiplier = ticketRack.remove(rackIndex);
        feedTimer = 0f;
        AudioManager.I().playCollect(feedingMultiplier);
    }

    private int unlockedTierCount() {
        int level = CompChipBar.I().getLevel();
        int count = 0;
        for (TicketTier tier : TIERS) {
            if (level >= tier.unlockLevel) count++;
        }
        return Math.max(1, count);
    }

    private void clampSelectedTier() {
        selectedTierIndex = MathUtils.clamp(
            selectedTierIndex, 0, unlockedTierCount() - 1);
    }

    private TicketTier selectedTier() {
        return TIERS.get(selectedTierIndex);
    }

    private float price(TicketTier tier) {
        float totalValue = 0f;
        for (com.avaricious.components.slot.Symbol symbol
            : com.avaricious.components.slot.Symbol.values()) {
            totalValue += SymbolValues.I().getValue(symbol);
        }
        float progressionScale = Math.max(1f, totalValue / 27f);
        return EconomyScaling.roundPrice(tier.basePrice * progressionScale);
    }

    private void updatePriceDisplay() {
        float nextPrice = price(selectedTier());
        if (Float.compare(nextPrice, displayedPrice) != 0) {
            displayedPrice = nextPrice;
            priceDisplay.setValue(nextPrice);
        }
        priceDisplay.getFirstDigitBounds().x =
            SELECTED_TICKET.x + SELECTED_TICKET.width / 2f
                - priceDisplay.getWidth() / 2f;
        priceDisplay.setColor(canBuySelectedTicket()
            ? Assets.I().yellow() : DISABLED_COLOR);
    }

    public void beginSpin() {
        activeMultiplier = loadedMultiplier;
        loadedMultiplier = 1;
        if (activeMultiplier > 1) {
            AudioManager.I().playUpgradeSelected();
            ScreenShake.I().addTrauma(
                Math.min(0.30f, 0.11f + activeMultiplier * 0.018f));
        }
    }

    public void finishSpin() {
        activeMultiplier = 1;
    }

    public float applyPayoutMultiplier(float points) {
        return points * activeMultiplier;
    }

    /** Automation hook: buys a chosen unlocked tier while preserving credits. */
    public boolean buyAutomatically(int multiplier, float creditReserve) {
        TicketTier tier = tierForMultiplier(multiplier);
        if (tier == null || CompChipBar.I().getLevel() < tier.unlockLevel
            || purchasingMultiplier != null
            || ticketRack.size() >= QUEUE_CAPACITY
            || ScoreDisplay.I().getScoreNumber() - price(tier) < creditReserve) {
            return false;
        }
        ScoreDisplay.I().removeFromScore(price(tier));
        ticketRack.add(tier.multiplier);
        purchasingMultiplier = tier.multiplier;
        purchaseTimer = 0f;
        return true;
    }

    /** Automation hook: loads the oldest stored ticket. */
    public boolean loadNextTicketAutomatically() {
        if (ticketRack.isEmpty() || loadedMultiplier > 1
            || feedingMultiplier != null || purchasingMultiplier != null) {
            return false;
        }
        loadTicket(0);
        return true;
    }

    private TicketTier tierForMultiplier(int multiplier) {
        for (TicketTier tier : TIERS) {
            if (tier.multiplier == multiplier) return tier;
        }
        return null;
    }

    private Rectangle spinBounds() {
        return spinBoundsSupplier == null ? null : spinBoundsSupplier.get();
    }

    private Rectangle scaledBounds(Rectangle source, float scale, float yOffset) {
        float width = source.width * scale;
        float height = source.height * scale;
        return new Rectangle(
            source.x + (source.width - width) / 2f,
            source.y + (source.height - height) / 2f + yOffset,
            width, height);
    }

    private static class TicketTier {
        private final int multiplier;
        private final int unlockLevel;
        private final float basePrice;

        private TicketTier(int multiplier, int unlockLevel, float basePrice) {
            this.multiplier = multiplier;
            this.unlockLevel = unlockLevel;
            this.basePrice = basePrice;
        }
    }
}
