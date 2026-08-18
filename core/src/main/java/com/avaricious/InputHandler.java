package com.avaricious;

import com.badlogic.gdx.math.Vector2;

public class InputHandler {

    private static InputHandler instance;
    public static InputHandler I() {
        return instance == null ? (instance = new InputHandler()) : instance;
    }

    private InputHandler() {
    }

    private final Vector2 mousePos = new Vector2();

    public void updateInputs() {
//        Input input = Gdx.input;
//        mousePos.set(input.getX(), input.getY());
//        ScreenManager.getViewport().unproject(mousePos);
//
//        boolean leftClick
    }

}
