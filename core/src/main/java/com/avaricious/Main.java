package com.avaricious;

import com.avaricious.screens.LoadingScreen;
import com.avaricious.screens.ScreenManager;
import com.avaricious.utility.DeviceInfo;
import com.avaricious.utility.GameContext;
import com.avaricious.utility.SeededRandomizer;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;


/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class Main extends Game {

    private SpriteBatch batch;
    private FitViewport viewport;
    private FitViewport uiViewport;
    private DeviceInfo deviceInfo;

    public Main(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public Main() {}

    @Override
    public void create() {
        SeededRandomizer.setSeed(MathUtils.random(1000, 9999));

        batch = new SpriteBatch();
        viewport = new FitViewport(16, 9);
        uiViewport = new FitViewport(1920, 1080);

        GameContext.init(batch, viewport, uiViewport, deviceInfo);
        ScreenManager.create(this).setScreen(LoadingScreen.class);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        Cursor emptyCursor = Gdx.graphics.newCursor(pixmap, 0, 0);
        Gdx.graphics.setCursor(emptyCursor);
        pixmap.dispose();
    }

    public FitViewport getViewport() {
        return viewport;
    }

    public FitViewport getUiViewport() {
        return uiViewport;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);

        Gdx.app.log("V", "width: " + viewport.getWorldWidth() + " height: " + viewport.getWorldHeight());
        Gdx.app.log("SV", "width: " + uiViewport.getWorldWidth() + " height: " + uiViewport.getWorldHeight());
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void resume() {
        super.resume();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

}
