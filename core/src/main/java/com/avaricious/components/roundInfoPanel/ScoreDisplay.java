package com.avaricious.components.roundInfoPanel;

import com.avaricious.CreditNumber;
import com.avaricious.DevTools;
import com.avaricious.components.DigitalNumber;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ScoreDisplay {

    private static ScoreDisplay instance;

    public static ScoreDisplay I() {
        return instance == null ? instance = new ScoreDisplay() : instance;
    }

    private final float DIGIT_Y = 6.5f;

    private final DigitalNumber scoreNumber = new DigitalNumber(0, Assets.I().lightColor(),
        new Rectangle(0.5f, DIGIT_Y, 7 / 12f, 11 / 12f), 0.75f)
        .setZIndex(ZIndex.BUTTON_BOARD);

    private final PropertyChangeSupport scoreChangeSupport = new PropertyChangeSupport(this);

    private ScoreDisplay() {
        scoreNumber.getIdleScaleEffect().setAllowed(false);
//        scoreNumber.getPulseEffect().setStrength(0.5f);
        scoreNumber.getPulseEffect().setSpeed(0.15f);
        setScoreNumber(300);
    }

    public void draw(float delta) {
        scoreNumber.draw(delta);
    }

    public void addToScore(float value) {
        setScoreNumber(getScoreNumber() + value);
    }

    public void removeFromScore(float value) {
        if (DevTools.unlimitedMoney()) return;
        setScoreNumber(getScoreNumber() - value);
    }

    public void setScoreNumber(float value) {
        float oldScore = getScoreNumber();
        scoreNumber.setValue(value);
        updateScoreXLayout();

        scoreChangeSupport.firePropertyChange("score", oldScore, scoreNumber.getValue());
    }

    public float getScoreNumber() {
        return scoreNumber.getValue();
    }

    private void updateScoreXLayout() {
        float screenCenterX = 2.5f;
        scoreNumber.getFirstDigitBounds().x = screenCenterX - scoreNumber.getWidth() / 2f;
    }

    public boolean reachedRoundGoal() {
        return scoreNumber.getValue() >= RoundInfoPanel.I().getReachNumber().getValue();
    }

    public void addScoreChangeListener(PropertyChangeListener listener) {
        scoreChangeSupport.addPropertyChangeListener(listener);
    }

}
