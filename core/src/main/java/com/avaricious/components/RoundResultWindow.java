package com.avaricious.components;

import com.avaricious.CreditNumber;
import com.avaricious.RoundStats;
import com.avaricious.audio.AudioManager;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.RunManager;
import com.avaricious.utility.RunSaveManager;
import com.avaricious.utility.TextureDrawing;
import com.avaricious.utility.VaultManager;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/** A short acknowledgement between rounds or before restarting a failed run. */
public final class RoundResultWindow {

    private static final float WORLD_WIDTH = 16f;
    private static final Rectangle ACTION_BUTTON_BOUNDS =
        new Rectangle(6.05f, 1.72f, 3.90f, 0.82f);
    private static final Rectangle VAULT_BUTTON_BOUNDS =
        new Rectangle(3.65f, 1.72f, 2.05f, 0.82f);
    private static final Rectangle SHORT_TERM_BOUNDS =
        new Rectangle(3.85f, 4.72f, 2.55f, 0.92f);
    private static final Rectangle MEDIUM_TERM_BOUNDS =
        new Rectangle(6.72f, 4.72f, 2.55f, 0.92f);
    private static final Rectangle LONG_TERM_BOUNDS =
        new Rectangle(9.60f, 4.72f, 2.55f, 0.92f);
    private static final Rectangle LESS_BUTTON_BOUNDS =
        new Rectangle(4.05f, 3.10f, 1.80f, 0.72f);
    private static final Rectangle MORE_BUTTON_BOUNDS =
        new Rectangle(10.15f, 3.10f, 1.80f, 0.72f);
    private static final Rectangle VAULT_BACK_BOUNDS =
        new Rectangle(3.85f, 1.72f, 2.20f, 0.82f);
    private static final Rectangle LOCK_BUTTON_BOUNDS =
        new Rectangle(7.00f, 1.72f, 5.15f, 0.82f);
    private static final Color GOLD = new Color(1f, 0.82f, 0.44f, 1f);
    private static final Color MUTED = new Color(0.63f, 0.71f, 0.76f, 1f);
    private static final Color WHITE = new Color(1f, 1f, 1f, 1f);
    private static final float STAT_CARD_WIDTH = 1.62f;
    private static final float STAT_LABEL_WIDTH = 1.36f;

    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final GeneratedFabledText clearedTitle = text("ROUND CLEARED", 18f, GOLD);
    private final GeneratedFabledText failedTitle = text(
        "OUT OF TIME", 18f, Assets.I().healthRedColor()
    );
    private final GeneratedFabledText cashLabel = text("CASH", 48f, MUTED);
    private final GeneratedFabledText billLabel = text("BILL", 48f, MUTED);
    private final GeneratedFabledText spinsLabel = statText("SPINS");
    private final GeneratedFabledText symbolsHitLabel = statText("SYMBOLS HIT");
    private final GeneratedFabledText symbolsCollectedLabel = statText("SYMBOLS COLLECTED");
    private final GeneratedFabledText moneyGainedLabel = statText("MONEY GAINED");
    private final GeneratedFabledText averageClaimLabel = statText("AVG CLAIM SEC");
    private final GeneratedFabledText payBillButton = text("PAY BILL", 39f, GOLD);
    private final GeneratedFabledText restartButton = text("START NEW RUN", 39f, MUTED);
    private final GeneratedFabledText vaultButton = text("VAULT", 43f, GOLD);
    private final GeneratedFabledText vaultTitle = text("VAULT", 18f, GOLD);
    private final GeneratedFabledText vaultPrompt = text(
        "LOCK CASH AND EARN INTEREST", 51f, MUTED
    );
    private final GeneratedFabledText fundsLocked = text(
        "FUNDS LOCKED", 38f, Assets.I().lightColor()
    );
    private final GeneratedFabledText amountLabel = text("AMOUNT", 46f, MUTED);
    private final GeneratedFabledText principalLabel = text("PRINCIPAL", 46f, MUTED);
    private final GeneratedFabledText returnLabel = text(
        "RETURN AT MATURITY", 46f, MUTED
    );
    private final GeneratedFabledText roundsLeftLabel = text(
        "ROUNDS LEFT", 46f, MUTED
    );
    private final GeneratedFabledText lessButton = text("LESS", 48f, MUTED);
    private final GeneratedFabledText moreButton = text("MORE", 48f, MUTED);
    private final GeneratedFabledText backButton = text("BACK", 45f, MUTED);
    private final GeneratedFabledText lockButton = text("LOCK CASH", 42f, GOLD);
    private final GeneratedFabledText threeRounds = text("THREE ROUNDS", 54f, MUTED);
    private final GeneratedFabledText fiveRounds = text("FIVE ROUNDS", 54f, MUTED);
    private final GeneratedFabledText sevenRounds = text("SEVEN ROUNDS", 54f, MUTED);
    private final GeneratedFabledText fifteenPercent = text(
        "FIFTEEN PERCENT", 65f, GOLD
    );
    private final GeneratedFabledText thirtyFivePercent = text(
        "THIRTY FIVE PERCENT", 65f, GOLD
    );
    private final GeneratedFabledText sixtyPercent = text(
        "SIXTY PERCENT", 65f, GOLD
    );

    private final CreditNumber cashNumber = new CreditNumber(
        0f, new Rectangle(0f, 5.05f, 0.28f, 0.44f), 0.34f
    ).setZIndex(ZIndex.SHOP_CARD);
    private final CreditNumber billNumber = new CreditNumber(
        0f, new Rectangle(0f, 5.05f, 0.28f, 0.44f), 0.34f
    ).setZIndex(ZIndex.SHOP_CARD);
    private final DigitalNumber spinsNumber = new DigitalNumber(
        0f, Assets.I().lightColor(),
        new Rectangle(0f, 3.20f, 0.22f, 0.35f), 0.27f
    ).setZIndex(ZIndex.SHOP_CARD);
    private final DigitalNumber symbolsHitNumber = new DigitalNumber(
        0f, Assets.I().lightColor(),
        new Rectangle(0f, 3.20f, 0.22f, 0.35f), 0.27f
    ).setZIndex(ZIndex.SHOP_CARD);
    private final DigitalNumber symbolsCollectedNumber = new DigitalNumber(
        0f, Assets.I().lightColor(),
        new Rectangle(0f, 3.20f, 0.22f, 0.35f), 0.27f
    ).setZIndex(ZIndex.SHOP_CARD);
    private final CreditNumber moneyGainedNumber = new CreditNumber(
        0f, new Rectangle(0f, 3.20f, 0.22f, 0.35f), 0.27f
    ).setZIndex(ZIndex.SHOP_CARD).setShowPositiveSign(true);
    private final DigitalNumber averageClaimNumber = new DigitalNumber(
        0f, Assets.I().lightColor(),
        new Rectangle(0f, 3.20f, 0.22f, 0.35f), 0.27f
    ).setAsDecimal().setZIndex(ZIndex.SHOP_CARD);
    private final CreditNumber vaultAmountNumber = new CreditNumber(
        0f, new Rectangle(0f, 3.18f, 0.27f, 0.42f), 0.33f
    ).setZIndex(ZIndex.SHOP_CARD);
    private final CreditNumber vaultReturnNumber = new CreditNumber(
        0f, new Rectangle(0f, 2.55f, 0.22f, 0.32f), 0.28f
    ).setZIndex(ZIndex.SHOP_CARD);
    private final DigitalNumber roundsLeftNumber = new DigitalNumber(
        0f, Assets.I().lightColor(),
        new Rectangle(0f, 2.18f, 0.24f, 0.38f), 0.30f
    ).setZIndex(ZIndex.SHOP_CARD);

    private boolean showing;
    private boolean cleared;
    private boolean inputArmed;
    private boolean buttonHovered;
    private boolean pressStartedOnButton;
    private boolean vaultButtonHovered;
    private boolean pressStartedOnVaultButton;
    private boolean vaultView;
    private float reservedBill;
    private float selectedAmount;
    private int selectedTerm = VaultManager.SHORT_TERM;
    private VaultControl hoveredVaultControl = VaultControl.NONE;
    private VaultControl pressedVaultControl = VaultControl.NONE;
    private Runnable action;

    private enum VaultControl {
        NONE,
        SHORT_TERM,
        MEDIUM_TERM,
        LONG_TERM,
        LESS,
        MORE,
        BACK,
        LOCK
    }

    public RoundResultWindow() {
        cashNumber.getIdleScaleEffect().setAllowed(false);
        billNumber.getIdleScaleEffect().setAllowed(false);
        billNumber.setColor(Assets.I().healthRedColor());
        spinsNumber.getIdleScaleEffect().setAllowed(false);
        symbolsHitNumber.getIdleScaleEffect().setAllowed(false);
        symbolsCollectedNumber.getIdleScaleEffect().setAllowed(false);
        moneyGainedNumber.getIdleScaleEffect().setAllowed(false);
        moneyGainedNumber.setColor(WHITE);
        averageClaimNumber.getIdleScaleEffect().setAllowed(false);
        vaultAmountNumber.getIdleScaleEffect().setAllowed(false);
        vaultReturnNumber.getIdleScaleEffect().setAllowed(false);
        roundsLeftNumber.getIdleScaleEffect().setAllowed(false);
    }

    private static GeneratedFabledText text(String value, float size, Color color) {
        GeneratedFabledText result = new GeneratedFabledText(
            value, size, 0.024f, 0.13f, ZIndex.SHOP_CARD, true
        );
        result.setFloatEffects(0f, 0f);
        result.getWords().forEach(word -> word.setColor(color));
        return result;
    }

    private static GeneratedFabledText statText(String value) {
        GeneratedFabledText result = text(value, 47f, MUTED);
        result.fitWithinWidth(STAT_LABEL_WIDTH);
        return result;
    }

    public void showCleared(float cash, float bill, Runnable action) {
        show(true, cash, bill, action);
    }

    public void showFailed(float cash, float bill, Runnable action) {
        show(false, cash, bill, action);
    }

    private void show(boolean cleared, float cash, float bill, Runnable action) {
        this.cleared = cleared;
        this.action = action;
        reservedBill = cleared ? Math.abs(bill) : 0f;
        cashNumber.setValue(cash);
        billNumber.setValue(-Math.abs(bill));
        RoundStats stats = RoundStats.I();
        spinsNumber.setValue(stats.getSpins());
        symbolsHitNumber.setValue(stats.getSymbolsHit());
        symbolsCollectedNumber.setValue(stats.getSymbolsCollected());
        moneyGainedNumber.setValue(stats.getMoneyGained());
        float averageClaim = Math.round(
            stats.getAverageCollectibleClaimTime() * 10f
        ) / 10f;
        averageClaimNumber.setValue(averageClaim);
        showing = true;
        vaultView = false;
        inputArmed = false;
        buttonHovered = false;
        pressStartedOnButton = false;
        vaultButtonHovered = false;
        pressStartedOnVaultButton = false;
        hoveredVaultControl = VaultControl.NONE;
        pressedVaultControl = VaultControl.NONE;
        updateVaultNumbers();
    }

    public void hide() {
        showing = false;
        inputArmed = false;
        buttonHovered = false;
        pressStartedOnButton = false;
        vaultButtonHovered = false;
        pressStartedOnVaultButton = false;
        vaultView = false;
        hoveredVaultControl = VaultControl.NONE;
        pressedVaultControl = VaultControl.NONE;
        action = null;
    }

    public boolean isShowing() {
        return showing;
    }

    public void handleInput(Vector2 mouse, boolean pressed, boolean wasPressed) {
        if (!showing) return;

        if (vaultView) {
            handleVaultInput(mouse, pressed, wasPressed);
            return;
        }

        buttonHovered = ACTION_BUTTON_BOUNDS.contains(mouse);
        vaultButtonHovered = cleared && VAULT_BUTTON_BOUNDS.contains(mouse);
        boolean keyboardDown = Gdx.input.isKeyPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyPressed(Input.Keys.ENTER);
        if (!inputArmed) {
            if (!pressed && !keyboardDown) inputArmed = true;
            return;
        }

        boolean keyboardPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
        if (pressed && !wasPressed) {
            pressStartedOnButton = buttonHovered;
            pressStartedOnVaultButton = vaultButtonHovered;
        }

        boolean buttonClicked = !pressed && wasPressed
            && pressStartedOnButton && buttonHovered;
        boolean vaultClicked = !pressed && wasPressed
            && pressStartedOnVaultButton && vaultButtonHovered;
        if (!pressed) {
            pressStartedOnButton = false;
            pressStartedOnVaultButton = false;
        }

        if (vaultClicked) {
            openVault();
        } else if (buttonClicked || keyboardPressed) {
            activateButton();
        }
    }

    private void openVault() {
        vaultView = true;
        inputArmed = false;
        selectedTerm = VaultManager.SHORT_TERM;
        selectedAmount = Math.min(
            availableToVault(),
            amountStep() * 5f
        );
        updateVaultNumbers();
        AudioManager.I().playHover();
    }

    private void handleVaultInput(
        Vector2 mouse,
        boolean pressed,
        boolean wasPressed
    ) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            closeVault();
            return;
        }

        hoveredVaultControl = vaultControlAt(mouse);
        boolean keyboardDown = Gdx.input.isKeyPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyPressed(Input.Keys.ENTER);
        if (!inputArmed) {
            if (!pressed && !keyboardDown) inputArmed = true;
            return;
        }

        if (pressed && !wasPressed) {
            pressedVaultControl = hoveredVaultControl;
        }

        if (!pressed && wasPressed) {
            VaultControl releasedControl = hoveredVaultControl;
            if (pressedVaultControl != VaultControl.NONE
                && pressedVaultControl == releasedControl) {
                activateVaultControl(releasedControl);
            }
            pressedVaultControl = VaultControl.NONE;
        }
    }

    private VaultControl vaultControlAt(Vector2 mouse) {
        if (VaultManager.I().hasActiveDeposit()) {
            return ACTION_BUTTON_BOUNDS.contains(mouse)
                ? VaultControl.BACK
                : VaultControl.NONE;
        }

        if (SHORT_TERM_BOUNDS.contains(mouse)) return VaultControl.SHORT_TERM;
        if (MEDIUM_TERM_BOUNDS.contains(mouse)) return VaultControl.MEDIUM_TERM;
        if (LONG_TERM_BOUNDS.contains(mouse)) return VaultControl.LONG_TERM;
        if (LESS_BUTTON_BOUNDS.contains(mouse)) return VaultControl.LESS;
        if (MORE_BUTTON_BOUNDS.contains(mouse)) return VaultControl.MORE;
        if (VAULT_BACK_BOUNDS.contains(mouse)) return VaultControl.BACK;
        if (LOCK_BUTTON_BOUNDS.contains(mouse)) return VaultControl.LOCK;
        return VaultControl.NONE;
    }

    private void activateVaultControl(VaultControl control) {
        switch (control) {
            case SHORT_TERM:
                selectedTerm = VaultManager.SHORT_TERM;
                break;
            case MEDIUM_TERM:
                selectedTerm = VaultManager.MEDIUM_TERM;
                break;
            case LONG_TERM:
                selectedTerm = VaultManager.LONG_TERM;
                break;
            case LESS:
                selectedAmount = Math.max(0f, selectedAmount - amountStep());
                break;
            case MORE:
                selectedAmount = Math.min(
                    availableToVault(),
                    selectedAmount + amountStep()
                );
                break;
            case BACK:
                closeVault();
                return;
            case LOCK:
                lockSelectedAmount();
                return;
            default:
                return;
        }

        updateVaultNumbers();
        AudioManager.I().playHover();
    }

    private void lockSelectedAmount() {
        if (selectedAmount <= 0f || selectedAmount > availableToVault()) {
            AudioManager.I().playMiss();
            return;
        }

        boolean deposited = VaultManager.I().deposit(
            selectedAmount,
            selectedTerm,
            RunManager.I().getRoundsManager().getCurrentRound()
        );
        if (!deposited) {
            AudioManager.I().playMiss();
            return;
        }

        cashNumber.setValue(ScoreDisplay.I().getScoreNumber());
        updateVaultNumbers();
        RunSaveManager.I().saveNow();
        AudioManager.I().playUpgradeSelected();
    }

    private void closeVault() {
        vaultView = false;
        inputArmed = false;
        hoveredVaultControl = VaultControl.NONE;
        pressedVaultControl = VaultControl.NONE;
        AudioManager.I().playHover();
    }

    private float availableToVault() {
        return Math.max(
            0f,
            ScoreDisplay.I().getScoreNumber() - reservedBill
        );
    }

    private float amountStep() {
        float available = availableToVault();
        if (available <= 0f) return 1f;
        double power = Math.ceil(Math.log10(Math.max(1f, available))) - 1d;
        return Math.max(1f, (float) Math.pow(10d, power));
    }

    private void updateVaultNumbers() {
        VaultManager vault = VaultManager.I();
        if (vault.hasActiveDeposit()) {
            vaultAmountNumber.setValue(vault.getPrincipal());
            vaultReturnNumber.setValue(vault.getProjectedPayout());
            roundsLeftNumber.setValue(vault.getRoundsRemaining(
                RunManager.I().getRoundsManager().getCurrentRound()
            ));
            return;
        }

        selectedAmount = Math.min(selectedAmount, availableToVault());
        vaultAmountNumber.setValue(selectedAmount);
        vaultReturnNumber.setValue(
            selectedAmount <= 0f
                ? 0f
                : com.avaricious.utility.EconomyScaling.roundPrice(
                    selectedAmount
                        * (1f + VaultManager.interestRate(selectedTerm))
                )
        );
    }

    private void activateButton() {
        Runnable next = action;
        hide();
        if (next != null) next.run();
    }

    public void draw(float delta) {
        if (!showing) return;

        rect(0f, 0f, WORLD_WIDTH, 9f,
            new Color(0.012f, 0.021f, 0.029f, 1f), 0.94f, ZIndex.SHOP);
        rect(3.25f, 1.42f, 9.50f, 6.15f,
            new Color(0.045f, 0.072f, 0.090f, 1f), 1f, ZIndex.SHOP);
        rect(3.25f, 7.52f, 9.50f, 0.05f,
            cleared ? GOLD : Assets.I().healthRedColor(), 0.85f, ZIndex.SHOP_CARD);

        if (vaultView) {
            drawVaultView(delta);
            return;
        }

        drawCentered(cleared ? clearedTitle : failedTitle, 6.82f, delta);
        drawCentered(cashLabel, 5.85f, -2.0f, delta);
        drawCentered(billLabel, 5.85f, 2.0f, delta);

        cashNumber.getFirstDigitBounds().x = 6f - cashNumber.getWidth() / 2f;
        billNumber.getFirstDigitBounds().x = 10f - billNumber.getWidth() / 2f;
        cashNumber.draw(delta);
        billNumber.draw(delta);

        rect(3.65f, 4.65f, 8.70f, 0.018f,
            MUTED, 0.20f, ZIndex.SHOP_CARD);
        drawStat(spinsLabel, spinsNumber, 4.56f, delta);
        drawStat(symbolsHitLabel, symbolsHitNumber, 6.28f, delta);
        drawStat(symbolsCollectedLabel, symbolsCollectedNumber, 8.00f, delta);
        drawStat(moneyGainedLabel, moneyGainedNumber, 9.72f, delta);
        drawStat(averageClaimLabel, averageClaimNumber, 11.44f, delta);

        drawActionButton(delta);
        if (cleared) drawVaultEntryButton(delta);
    }

    private void drawVaultView(float delta) {
        drawCentered(vaultTitle, 6.82f, delta);
        drawCentered(vaultPrompt, 6.28f, delta);

        if (VaultManager.I().hasActiveDeposit()) {
            drawActiveDeposit(delta);
            return;
        }

        drawTermButton(
            SHORT_TERM_BOUNDS,
            threeRounds,
            fifteenPercent,
            VaultManager.SHORT_TERM,
            delta
        );
        drawTermButton(
            MEDIUM_TERM_BOUNDS,
            fiveRounds,
            thirtyFivePercent,
            VaultManager.MEDIUM_TERM,
            delta
        );
        drawTermButton(
            LONG_TERM_BOUNDS,
            sevenRounds,
            sixtyPercent,
            VaultManager.LONG_TERM,
            delta
        );

        drawCentered(amountLabel, 4.05f, delta);
        vaultAmountNumber.getFirstDigitBounds().y = 3.18f;
        centerNumber(vaultAmountNumber);
        vaultAmountNumber.draw(delta);
        drawControlButton(
            LESS_BUTTON_BOUNDS,
            lessButton,
            hoveredVaultControl == VaultControl.LESS,
            selectedAmount > 0f,
            MUTED,
            delta
        );
        drawControlButton(
            MORE_BUTTON_BOUNDS,
            moreButton,
            hoveredVaultControl == VaultControl.MORE,
            selectedAmount < availableToVault(),
            MUTED,
            delta
        );

        drawCentered(returnLabel, 2.95f, delta);
        vaultReturnNumber.getFirstDigitBounds().y = 2.55f;
        centerNumber(vaultReturnNumber);
        vaultReturnNumber.draw(delta);

        drawControlButton(
            VAULT_BACK_BOUNDS,
            backButton,
            hoveredVaultControl == VaultControl.BACK,
            true,
            MUTED,
            delta
        );
        drawControlButton(
            LOCK_BUTTON_BOUNDS,
            lockButton,
            hoveredVaultControl == VaultControl.LOCK,
            selectedAmount > 0f,
            GOLD,
            delta
        );
    }

    private void drawActiveDeposit(float delta) {
        drawCentered(fundsLocked, 5.72f, delta);
        drawCentered(principalLabel, 5.08f, delta);
        vaultAmountNumber.getFirstDigitBounds().y = 4.42f;
        centerNumber(vaultAmountNumber);
        vaultAmountNumber.draw(delta);

        drawCentered(returnLabel, 3.80f, delta);
        vaultReturnNumber.getFirstDigitBounds().y = 3.14f;
        centerNumber(vaultReturnNumber);
        vaultReturnNumber.draw(delta);

        drawCentered(roundsLeftLabel, 2.65f, delta);
        roundsLeftNumber.getFirstDigitBounds().y = 2.05f;
        centerNumber(roundsLeftNumber);
        roundsLeftNumber.draw(delta);

        drawControlButton(
            ACTION_BUTTON_BOUNDS,
            backButton,
            hoveredVaultControl == VaultControl.BACK,
            true,
            MUTED,
            delta
        );
    }

    private void drawTermButton(
        Rectangle bounds,
        GeneratedFabledText termLabel,
        GeneratedFabledText interestLabel,
        int term,
        float delta
    ) {
        boolean selected = selectedTerm == term;
        boolean hovered = hoveredVaultControl == controlForTerm(term);
        Color fill = hovered
            ? new Color(0.14f, 0.21f, 0.25f, 1f)
            : new Color(0.07f, 0.11f, 0.14f, 1f);

        rect(bounds.x + 0.05f, bounds.y - 0.06f,
            bounds.width, bounds.height,
            Color.BLACK, 0.50f, ZIndex.SHOP_CARD);
        rect(bounds.x, bounds.y, bounds.width, bounds.height,
            fill, 1f, ZIndex.SHOP_CARD);
        rect(bounds.x, bounds.y + bounds.height - 0.045f,
            bounds.width, 0.045f,
            GOLD, selected ? 1f : 0.24f, ZIndex.SHOP_CARD);

        drawCenteredInBounds(termLabel, bounds, bounds.y + 0.52f, delta);
        drawCenteredInBounds(interestLabel, bounds, bounds.y + 0.18f, delta);
    }

    private VaultControl controlForTerm(int term) {
        if (term == VaultManager.SHORT_TERM) return VaultControl.SHORT_TERM;
        if (term == VaultManager.MEDIUM_TERM) return VaultControl.MEDIUM_TERM;
        return VaultControl.LONG_TERM;
    }

    private void drawVaultEntryButton(float delta) {
        drawControlButton(
            VAULT_BUTTON_BOUNDS,
            vaultButton,
            vaultButtonHovered && inputArmed,
            true,
            GOLD,
            delta
        );
    }

    private void drawControlButton(
        Rectangle bounds,
        GeneratedFabledText label,
        boolean hovered,
        boolean enabled,
        Color accent,
        float delta
    ) {
        Color fill = hovered && enabled
            ? new Color(0.16f, 0.23f, 0.27f, 1f)
            : new Color(0.09f, 0.13f, 0.16f, 1f);
        rect(bounds.x + 0.05f, bounds.y - 0.07f,
            bounds.width, bounds.height,
            Color.BLACK, 0.55f, ZIndex.SHOP_CARD);
        rect(bounds.x, bounds.y, bounds.width, bounds.height,
            fill, enabled ? 1f : 0.42f, ZIndex.SHOP_CARD);
        rect(bounds.x, bounds.y + bounds.height - 0.045f,
            bounds.width, 0.045f,
            accent, enabled ? 0.92f : 0.20f, ZIndex.SHOP_CARD);

        label.fitWithinWidth(bounds.width - 0.25f);
        drawCenteredInBounds(label, bounds, bounds.y + 0.25f, delta);
    }

    private void drawCenteredInBounds(
        GeneratedFabledText text,
        Rectangle bounds,
        float y,
        float delta
    ) {
        text.setAbsoluteX(
            bounds.x + (bounds.width - text.getRenderedWidth()) / 2f
        );
        text.setY(y);
        text.draw(delta);
    }

    private void centerNumber(DigitalNumber number) {
        number.getFirstDigitBounds().x = WORLD_WIDTH / 2f
            - number.getWidth() / 2f;
    }

    private void drawStat(
        GeneratedFabledText label,
        DigitalNumber number,
        float centerX,
        float delta
    ) {
        rect(centerX - STAT_CARD_WIDTH / 2f, 2.92f, STAT_CARD_WIDTH, 1.42f,
            new Color(0.026f, 0.046f, 0.060f, 1f), 0.88f, ZIndex.SHOP_CARD);
        label.setAbsoluteX(centerX - label.getRenderedWidth() / 2f);
        label.setY(3.91f);
        label.draw(delta);
        number.getFirstDigitBounds().x = centerX - number.getWidth() / 2f;
        number.draw(delta);
    }

    private void drawActionButton(float delta) {
        Color accent = cleared ? GOLD : Assets.I().healthRedColor();
        Color fill = buttonHovered && inputArmed
            ? new Color(0.16f, 0.23f, 0.27f, 1f)
            : new Color(0.09f, 0.13f, 0.16f, 1f);

        rect(
            ACTION_BUTTON_BOUNDS.x + 0.06f,
            ACTION_BUTTON_BOUNDS.y - 0.08f,
            ACTION_BUTTON_BOUNDS.width,
            ACTION_BUTTON_BOUNDS.height,
            Color.BLACK,
            0.55f,
            ZIndex.SHOP_CARD
        );
        rect(
            ACTION_BUTTON_BOUNDS.x,
            ACTION_BUTTON_BOUNDS.y,
            ACTION_BUTTON_BOUNDS.width,
            ACTION_BUTTON_BOUNDS.height,
            fill,
            1f,
            ZIndex.SHOP_CARD
        );
        rect(
            ACTION_BUTTON_BOUNDS.x,
            ACTION_BUTTON_BOUNDS.y,
            ACTION_BUTTON_BOUNDS.width,
            0.05f,
            accent,
            0.95f,
            ZIndex.SHOP_CARD
        );
        drawCentered(cleared ? payBillButton : restartButton, 1.98f, delta);
    }

    private void drawCentered(GeneratedFabledText text, float y, float delta) {
        text.setAbsoluteX((WORLD_WIDTH - text.getRenderedWidth()) / 2f);
        text.setY(y);
        text.draw(delta);
    }

    private void drawCentered(
        GeneratedFabledText text,
        float y,
        float xOffset,
        float delta
    ) {
        text.setAbsoluteX((WORLD_WIDTH - text.getRenderedWidth()) / 2f + xOffset);
        text.setY(y);
        text.draw(delta);
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
}
