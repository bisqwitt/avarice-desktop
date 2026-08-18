package com.avaricious.components.roundInfoPanel;

import com.avaricious.components.DigitalNumber;
import com.avaricious.utility.Assets;
import com.avaricious.utility.GameContext;
import com.badlogic.gdx.math.Rectangle;

public class PlayerHealths {

    private static PlayerHealths instance;

    public static PlayerHealths I() {
        return instance == null ? instance = new PlayerHealths() : instance;
    }

    private final DigitalNumber playerHealth = new DigitalNumber(100, Assets.I().healthRedColor(), 3,
        new Rectangle(0.5f, 17f, 7 / 17f, 11 / 17f), 0.6f);
    private final DigitalNumber enemyHealth = new DigitalNumber(100, Assets.I().healthRedColor(), 3,
        new Rectangle(6, 17f, 7 / 17f, 11 / 17f), 0.6f);

    private PlayerHealths() {
        playerHealth.getIdleScaleEffect().setAllowed(false);
        enemyHealth.getIdleScaleEffect().setAllowed(false);

        updateEnemyHealthX();
    }

    public void draw(float delta) {
//        playerHealth.draw(delta);
//        enemyHealth.draw(delta);
    }

    public void setPlayerHealth(int value) {
        playerHealth.setValue(value);
    }

    public void setEnemyHealth(int value) {
        enemyHealth.setValue(value);
        updateEnemyHealthX();
    }

    public float getPlayerHealth() {
        return playerHealth.getValue();
    }

    public float getEnemyHealth() {
        return enemyHealth.getValue();
    }

    private void updateEnemyHealthX() {
        float screenWidth = GameContext.I().viewport.getWorldWidth();
        float width = enemyHealth.getWidth();
        enemyHealth.getFirstDigitBounds().x = screenWidth - width - 0.5f;
    }

}
