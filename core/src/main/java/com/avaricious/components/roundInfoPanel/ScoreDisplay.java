package com.avaricious.components.roundInfoPanel;

import com.avaricious.CreditNumber;
import com.avaricious.DevTools;
import com.avaricious.components.texts.GeneratedFabledText;
import com.avaricious.utility.Assets;
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

    private static final float DIGIT_Y = 7.08f + GameplayLayout.HUD_Y_OFFSET;
    private static final float LABEL_Y = 8.17f + GameplayLayout.HUD_Y_OFFSET;

    private final GeneratedFabledText scoreLabel = new GeneratedFabledText(
        "CASH", 46f, 0.022f, 0.11f, ZIndex.BUTTON_BOARD, false);

    private final CreditNumber scoreNumber = new CreditNumber(0,
        new Rectangle(GameplayLayout.HUD_LEFT, DIGIT_Y, 7 / 12f, 11 / 12f), 0.75f)
        .setZIndex(ZIndex.BUTTON_BOARD);

    private final PropertyChangeSupport scoreChangeSupport = new PropertyChangeSupport(this);

    private ScoreDisplay() {
        scoreLabel.setAbsoluteX(GameplayLayout.HUD_LEFT);
        scoreLabel.setY(LABEL_Y);
        scoreLabel.getWords().forEach(word -> word.setColor(Assets.I().silver()));
        scoreNumber.setColor(Assets.I().lightColor());
        scoreNumber.getIdleScaleEffect().setAllowed(false);
//        scoreNumber.getPulseEffect().setStrength(0.5f);
        scoreNumber.getPulseEffect().setSpeed(0.15f);
        setScoreNumber(0);
    }

    public void draw(float delta) {
        scoreLabel.draw(delta);
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
        scoreNumber.getFirstDigitBounds().x = GameplayLayout.HUD_LEFT;
    }

    public Rectangle getCollisionBounds() {
        float padding = 0.10f;
        float contentWidth = Math.max(
            scoreLabel.getNaturalWidth(),
            scoreNumber.getWidth()
        );
        float bottom = DIGIT_Y - padding;
        float top = LABEL_Y + 0.32f;

        return new Rectangle(
            GameplayLayout.HUD_LEFT - padding,
            bottom,
            contentWidth + padding * 2f,
            top - bottom
        );
    }

    public void addScoreChangeListener(PropertyChangeListener listener) {
        scoreChangeSupport.addPropertyChangeListener(listener);
    }

    public boolean isPulsing() {
        return scoreNumber.getPulseEffect().isActive();
    }

}
