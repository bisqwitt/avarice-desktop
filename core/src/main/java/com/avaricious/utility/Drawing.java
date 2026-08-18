package com.avaricious.utility;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface Drawing {

    void draw(SpriteBatch batch);

    ZIndex getZIndex();

}
