package com.avaricious.components.roundInfoPanel;

import com.avaricious.CreditNumber;
import com.avaricious.DevTools;
import com.avaricious.RoundStats;
import com.avaricious.utility.GameplayLayout;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ScoreDisplay {

    private static ScoreDisplay instance;

    public static ScoreDisplay I() {
        return instance == null ? instance = new ScoreDisplay() : instance;
    }

    private static final float DIGIT_Y = GameplayLayout.CASH_Y + 0.035f;

    private final CreditNumber scoreNumber = new CreditNumber(
        0,
        new Rectangle(GameplayLayout.CASH_LEFT, DIGIT_Y, 0.32f, 0.50f),
        0.38f
    )
        .setZIndex(ZIndex.BUTTON_BOARD);

    private final PropertyChangeSupport scoreChangeSupport = new PropertyChangeSupport(this);

    private ScoreDisplay() {
        scoreNumber.setCompactThreshold(1_000f);
        scoreNumber.getIdleScaleEffect().setAllowed(false);
        scoreNumber.getPulseEffect().setStrength(0.35f);
        scoreNumber.getPulseEffect().setSpeed(0.09f);
        setScoreNumber(0);
    }

    public void draw(float delta) {
        scoreNumber.draw(delta);
    }

    public void addToScore(float value) {
        RoundStats.I().recordMoneyGained(value);
        setScoreNumber(getScoreNumber() + value);
    }

    public void removeFromScore(float value) {
        if (DevTools.unlimitedMoney()) return;
        setScoreNumber(getScoreNumber() - value);
    }

    public void setScoreNumber(float value) {
        float oldScore = getScoreNumber();
        scoreNumber.setValue(value);

        scoreChangeSupport.firePropertyChange("score", oldScore, scoreNumber.getValue());
    }

    public float getScoreNumber() {
        return scoreNumber.getValue();
    }

    public Rectangle getCollisionBounds() {
        float padding = 0.10f;

        return new Rectangle(
            GameplayLayout.CASH_LEFT - padding,
            GameplayLayout.CASH_Y - padding,
            GameplayLayout.CASH_WIDTH + padding * 2f,
            0.56f + padding * 2f
        );
    }

    public void addScoreChangeListener(PropertyChangeListener listener) {
        scoreChangeSupport.addPropertyChangeListener(listener);
    }

    public boolean isPulsing() {
        return scoreNumber.getPulseEffect().isActive();
    }

}
