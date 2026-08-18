package com.avaricious.components.roundInfoPanel;

import com.avaricious.components.DigitalNumber;
import com.avaricious.components.texts.SpinsWord;
import com.avaricious.utility.Assets;
import com.avaricious.utility.ZIndex;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class AutoSpinDisplay {

    private static AutoSpinDisplay instance;

    public static AutoSpinDisplay I() {
        return instance == null ? instance = new AutoSpinDisplay() : instance;
    }

    private final SpinsWord spinsText = new SpinsWord(
        new Vector2(5.25f, 0.6f), 28f, 0.06f, ZIndex.BUTTON_BOARD
    );

    private final DigitalNumber autoSpins = new DigitalNumber(0, Assets.I().lightColor(),
        new Rectangle(7f, 0.6f, 7 / 28f, 11 / 28f), 0.3f);

    private boolean showing = false;

    public void show() {
        showing = true;
    }

    public void draw(float delta) {
        if (!showing) return;
        spinsText.draw(delta);
        autoSpins.draw(delta);
    }

    public void addSpin() {
        autoSpins.setValue(autoSpins.getValue() + 1);
    }

    public void removeSpin() {
        autoSpins.setValue(autoSpins.getValue() - 1);
    }

    public float getSpins() {
        return autoSpins.getValue();
    }
}
