package com.avaricious.components.roundInfoPanel;

import com.avaricious.CreditNumber;
import com.avaricious.DevTools;
import com.avaricious.game.run.RoundStats;
import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.game.run.CashBalance;
import com.avaricious.utility.GameplayLayout;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

import java.beans.PropertyChangeListener;

public class ScoreDisplay {

    private static ScoreDisplay instance;
    private static CashBalance configuredBalance;
    private static RoundStats configuredRoundStats;

    public static void configure(
        CashBalance balance,
        RoundStats roundStats
    ) {
        if (instance != null) {
            throw new IllegalStateException("ScoreDisplay is already initialized");
        }
        configuredBalance = balance;
        configuredRoundStats = roundStats;
    }

    public static ScoreDisplay I() {
        if (configuredBalance == null) {
            throw new IllegalStateException(
                "ScoreDisplay must be configured with a run cash balance"
            );
        }
        return instance == null
            ? instance = new ScoreDisplay(
                configuredBalance,
                configuredRoundStats
            )
            : instance;
    }

    private static final float DIGIT_WIDTH = 0.39f;
    private static final float DIGIT_HEIGHT = 0.60f;
    private static final float DIGIT_SPACING = 0.46f;
    private static final float DIGIT_Y = GameplayLayout.CASH_Y + 0.035f;
    private static final float LABEL_Y = DIGIT_Y + DIGIT_HEIGHT + 0.09f;
    private static final float LABEL_HEIGHT = 0.24f;
    private static final Color LABEL_COLOR = new Color(0.63f, 0.71f, 0.76f, 1f);

    private final GeneratedFabledText cashLabel = new GeneratedFabledText(
        "CASH", 54f, 0.018f, 0.10f, ZIndex.BUTTON_BOARD
    );

    private final CreditNumber scoreNumber = new CreditNumber(
        0,
        new Rectangle(GameplayLayout.CASH_LEFT, DIGIT_Y, DIGIT_WIDTH, DIGIT_HEIGHT),
        DIGIT_SPACING
    )
        .setZIndex(ZIndex.BUTTON_BOARD);

    private final CashBalance balance;
    private final RoundStats roundStats;

    private ScoreDisplay(CashBalance balance, RoundStats roundStats) {
        this.balance = balance;
        this.roundStats = roundStats;
        cashLabel.setFloatEffects(0f, 0f);
        cashLabel.getWords().forEach(word -> word.setColor(LABEL_COLOR));
        cashLabel.setAbsoluteX(GameplayLayout.CASH_LEFT);
        cashLabel.setY(LABEL_Y);

        scoreNumber.setCompactThreshold(1_000f);
        scoreNumber.getIdleScaleEffect().setAllowed(false);
        scoreNumber.getPulseEffect().setStrength(0.35f);
        scoreNumber.getPulseEffect().setSpeed(0.09f);
        scoreNumber.setValue(balance.get());
        balance.onChange(event -> scoreNumber.setValue(
            ((Number) event.getNewValue()).floatValue()
        ));
    }

    public void draw(float delta) {
        cashLabel.draw(delta);
        scoreNumber.draw(delta);
    }

    public void addToScore(float value) {
        roundStats.recordMoneyGained(value);
        balance.add(value);
    }

    public void removeFromScore(float value) {
        if (DevTools.unlimitedMoney()) return;
        balance.subtract(value);
    }

    public void setScoreNumber(float value) {
        balance.set(value);
    }

    public float getScoreNumber() {
        return balance.get();
    }

    public Rectangle getCollisionBounds() {
        float padding = 0.10f;

        return new Rectangle(
            GameplayLayout.CASH_LEFT - padding,
            GameplayLayout.CASH_Y - padding,
            GameplayLayout.CASH_WIDTH + padding * 2f,
            LABEL_Y + LABEL_HEIGHT - GameplayLayout.CASH_Y + padding * 2f
        );
    }

    public void addScoreChangeListener(PropertyChangeListener listener) {
        balance.onChange(listener);
    }

    public boolean isPulsing() {
        return scoreNumber.getPulseEffect().isActive();
    }

}
