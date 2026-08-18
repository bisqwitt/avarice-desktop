package com.avaricious.screens.temp;

import com.avaricious.components.background.WarpBackground;
import com.avaricious.screens.ScreenLayer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class BackgroundLayer extends ScreenLayer {

    private final WarpBackground background = new WarpBackground();

    private final World world = new World(new Vector2(0, 0), true);
//    private final RayHandler rayHandler = new RayHandler(world);
//    private final BackgroundLights backgroundLights = new BackgroundLights(rayHandler);

    @Override
    public void init() {
//        rayHandler.setAmbientLight(1f);
    }

    @Override
    public void handleInput() {
    }

    @Override
    public void render(SpriteBatch batch, float delta) {
//        rayHandler.updateAndRender();
        background.render(batch, delta);
    }
}
