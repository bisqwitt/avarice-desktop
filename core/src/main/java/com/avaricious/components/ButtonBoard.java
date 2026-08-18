package com.avaricious.components;

import static com.badlogic.gdx.math.MathUtils.lerp;

import com.avaricious.components.buttons.BuySpinButton;
import com.avaricious.components.buttons.DisablableButton;
import com.avaricious.components.buttons.DrawCardButton;
import com.avaricious.components.buttons.SpinButton;
import com.avaricious.components.roundInfoPanel.AutoSpinDisplay;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.slot.SlotMachine;
import com.avaricious.items.upgrades.Hand;
import com.avaricious.screens.ScreenManager;
import com.avaricious.screens.SlotScreen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class ButtonBoard {

    private static ButtonBoard instance;

    public static ButtonBoard I() {
        return instance == null ? instance = new ButtonBoard() : instance;
    }

    private final float BOARD_X = 0.75f;
    private final float BOARD_Y = 0.75f;

    private final float BUTTON_W = 79 / 35f;
    private final float BUTTON_H = 25 / 35f;

    private SpinButton spinAgainButton;
    private BuySpinButton buySpinButton;
    private DrawCardButton drawCardButton;
    private DisablableButton spinButton;

    private boolean isMoving = false;

    private final float moveDistance = 5f;
    private final float moveDuration = 0.45f;

    private float moveTime = 0f;

    private float startY;
    private float targetY;

    private ButtonBoard() {
    }

    public ButtonBoard init(Runnable onSpinButtonPressed, Runnable onCashoutButtonPressed) {
        spinAgainButton = new SpinButton(onSpinButtonPressed,
            new Rectangle(BOARD_X + 12f, BOARD_Y, BUTTON_W, BUTTON_H), Input.Keys.SPACE);
        buySpinButton = new BuySpinButton(() -> {
            AutoSpinDisplay.I().addSpin();
            ScoreDisplay.I().removeFromScore(50);
            if (AutoSpinDisplay.I().getSpins() == 1 && SlotMachine.I().isStale())
                ScreenManager.I().getScreen(SlotScreen.class).onSpinButtonPressed();
        },
            new Rectangle(BOARD_X + 12f, BOARD_Y, BUTTON_W, BUTTON_H), Input.Keys.SPACE);

        drawCardButton = new DrawCardButton(() -> {
            Hand.I().drawCard();
            ScoreDisplay.I().removeFromScore(25);
        },
            new Rectangle(BOARD_X + 0.2f, BOARD_Y, BUTTON_W, BUTTON_H), Input.Keys.ENTER);

        spinButton = spinAgainButton;
        return this;
    }

    public void handleInput(Vector2 mouse, boolean leftClickPressed, boolean leftClickWasPressed) {
        spinButton.handleInput(mouse, leftClickPressed, leftClickWasPressed);
//        drawCardButton.handleInput(mouse, leftClickPressed, leftClickWasPressed);
    }

    public void draw(float delta) {
        updateMovement(delta);

        spinButton.draw(delta);
//        drawCardButton.draw(delta);
    }

    public void moveOut() {
        startY = spinAgainButton.getBounds().y;
        targetY = startY - moveDistance;
        moveTime = 0f;
        isMoving = true;
    }

    public void moveIn() {
        startY = spinAgainButton.getBounds().y;
        targetY = startY + moveDistance;
        moveTime = 0f;
        isMoving = true;
    }

    private void updateMovement(float delta) {
        if (!isMoving) return;

        moveTime += delta;
        float progress = Math.min(moveTime / moveDuration, 1f);
        float eased = Interpolation.smooth.apply(progress);

        spinButton.getBounds().y = lerp(startY, targetY, eased);
        drawCardButton.getBounds().y = lerp(startY, targetY, eased);

        if (progress >= 1f) {
            spinButton.getBounds().y = targetY;
            drawCardButton.getBounds().y = targetY;
            isMoving = false;
        }
    }

    public void activateAutoSpin() {
        buySpinButton.setVisibleAnimated(true);
        spinButton = buySpinButton;
    }

    public void setVisible(boolean visible) {
        spinAgainButton.setVisibleAnimated(visible);
        buySpinButton.setVisibleAnimated(visible);
        drawCardButton.setVisibleAnimated(visible);
    }

    public boolean isVisible() {
        return drawCardButton.isVisibleNow();
    }
}
