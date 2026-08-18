package com.avaricious.components.roundInfoPanel;

import com.avaricious.components.DigitalNumber;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameContext;
import com.badlogic.gdx.math.Rectangle;

public class PlayerScores {

    private static PlayerScores instance;

    public static PlayerScores I() {
        return instance == null ? instance = new PlayerScores() : instance;
    }

    private final DigitalNumber playerScoreNumber = new DigitalNumber(0, Assets.I().lightColor(), 1,
        new Rectangle(1f, 16f, 7 / 14f, 11 / 14f), 0.55f);
    private final DigitalNumber enemyScoreNumber = new DigitalNumber(0, Assets.I().lightColor(), 1,
        new Rectangle(6, 16f, 7 / 14f, 11 / 14f), 0.55f);

    private PlayerScores() {
        playerScoreNumber.getIdleScaleEffect().setAllowed(false);
        enemyScoreNumber.getIdleScaleEffect().setAllowed(false);

        updateEnemyScoreNumberX();
    }

    public void draw(float delta) {
//        playerScoreNumber.draw(delta);
//        enemyScoreNumber.draw(delta);
    }

    public void setPlayerScoreNumber(int value) {
        playerScoreNumber.setValue(value);
    }

    public void setEnemyScoreNumber(int value) {
        enemyScoreNumber.setValue(value);
        updateEnemyScoreNumberX();
    }

    private void updateEnemyScoreNumberX() {
        float screenWidth = GameContext.I().viewport.getWorldWidth();
        float width = enemyScoreNumber.getWidth();
        enemyScoreNumber.getFirstDigitBounds().x = screenWidth - width - 1f;
    }

    public float getPlayerScore() {
        return playerScoreNumber.getValue();
    }

    public float getEnemyScore() {
        return enemyScoreNumber.getValue();
    }
}
