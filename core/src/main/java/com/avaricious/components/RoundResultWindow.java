package com.avaricious.components;

import com.avaricious.CreditNumber;
import com.avaricious.RoundStats;
import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.utility.AssetKey;
import com.avaricious.utility.Assets;
import com.avaricious.utility.Pencil;
import com.avaricious.utility.TextureDrawing;
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
    private static final Color GOLD = new Color(1f, 0.82f, 0.44f, 1f);
    private static final Color MUTED = new Color(0.63f, 0.71f, 0.76f, 1f);

    private final TextureRegion whitePixel = Assets.I().get(AssetKey.WHITE_PIXEL);
    private final GeneratedFabledText clearedTitle = text("ROUND CLEARED", 18f, GOLD);
    private final GeneratedFabledText failedTitle = text(
        "OUT OF TIME", 18f, Assets.I().healthRedColor()
    );
    private final GeneratedFabledText cashLabel = text("CASH", 48f, MUTED);
    private final GeneratedFabledText billLabel = text("BILL", 48f, MUTED);
    private final GeneratedFabledText spinsLabel = statText("SPINS");
    private final GeneratedFabledText symbolsHitLabel = statText("SYMBOLS HIT");
    private final GeneratedFabledText moneyGainedLabel = statText("MONEY GAINED");
    private final GeneratedFabledText averageClaimLabel = statText("AVG CLAIM SEC");
    private final GeneratedFabledText payBillButton = text("PAY BILL", 39f, GOLD);
    private final GeneratedFabledText restartButton = text("START NEW RUN", 39f, MUTED);

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
    private final CreditNumber moneyGainedNumber = new CreditNumber(
        0f, new Rectangle(0f, 3.20f, 0.22f, 0.35f), 0.27f
    ).setZIndex(ZIndex.SHOP_CARD);
    private final DigitalNumber averageClaimNumber = new DigitalNumber(
        0f, Assets.I().lightColor(),
        new Rectangle(0f, 3.20f, 0.22f, 0.35f), 0.27f
    ).setAsDecimal().setZIndex(ZIndex.SHOP_CARD);

    private boolean showing;
    private boolean cleared;
    private boolean inputArmed;
    private boolean buttonHovered;
    private boolean pressStartedOnButton;
    private Runnable action;

    public RoundResultWindow() {
        cashNumber.getIdleScaleEffect().setAllowed(false);
        billNumber.getIdleScaleEffect().setAllowed(false);
        spinsNumber.getIdleScaleEffect().setAllowed(false);
        symbolsHitNumber.getIdleScaleEffect().setAllowed(false);
        moneyGainedNumber.getIdleScaleEffect().setAllowed(false);
        averageClaimNumber.getIdleScaleEffect().setAllowed(false);
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
        result.fitWithinWidth(1.72f);
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
        cashNumber.setValue(cash);
        billNumber.setValue(bill);
        RoundStats stats = RoundStats.I();
        spinsNumber.setValue(stats.getSpins());
        symbolsHitNumber.setValue(stats.getSymbolsHit());
        moneyGainedNumber.setValue(stats.getMoneyGained());
        float averageClaim = Math.round(
            stats.getAverageCollectibleClaimTime() * 10f
        ) / 10f;
        averageClaimNumber.setValue(averageClaim);
        showing = true;
        inputArmed = false;
        buttonHovered = false;
        pressStartedOnButton = false;
    }

    public void hide() {
        showing = false;
        inputArmed = false;
        buttonHovered = false;
        pressStartedOnButton = false;
        action = null;
    }

    public boolean isShowing() {
        return showing;
    }

    public void handleInput(Vector2 mouse, boolean pressed, boolean wasPressed) {
        if (!showing) return;

        buttonHovered = ACTION_BUTTON_BOUNDS.contains(mouse);
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
        }

        boolean buttonClicked = !pressed && wasPressed
            && pressStartedOnButton && buttonHovered;
        if (!pressed) pressStartedOnButton = false;

        if (buttonClicked || keyboardPressed) {
            activateButton();
        }
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

        drawCentered(cleared ? clearedTitle : failedTitle, 6.82f, delta);
        drawCentered(cashLabel, 5.85f, -2.0f, delta);
        drawCentered(billLabel, 5.85f, 2.0f, delta);

        cashNumber.getFirstDigitBounds().x = 6f - cashNumber.getWidth() / 2f;
        billNumber.getFirstDigitBounds().x = 10f - billNumber.getWidth() / 2f;
        cashNumber.draw(delta);
        billNumber.draw(delta);

        rect(3.65f, 4.65f, 8.70f, 0.018f,
            MUTED, 0.20f, ZIndex.SHOP_CARD);
        drawStat(spinsLabel, spinsNumber, 4.70f, delta);
        drawStat(symbolsHitLabel, symbolsHitNumber, 6.90f, delta);
        drawStat(moneyGainedLabel, moneyGainedNumber, 9.10f, delta);
        drawStat(averageClaimLabel, averageClaimNumber, 11.30f, delta);

        drawActionButton(delta);
    }

    private void drawStat(
        GeneratedFabledText label,
        DigitalNumber number,
        float centerX,
        float delta
    ) {
        rect(centerX - 1.02f, 2.92f, 2.04f, 1.42f,
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
