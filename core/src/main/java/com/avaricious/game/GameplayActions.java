package com.avaricious.game;

/** Commands cards and automations may request from the active gameplay screen. */
public interface GameplayActions {

    void requestSpin();

    void selectCardToDiscard();
}
