package com.avaricious.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class ScreenLayer {

    public abstract void init();

    public abstract void handleInput();

    public abstract void render(SpriteBatch batch,  float delta);

}
