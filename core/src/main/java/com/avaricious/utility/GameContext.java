package com.avaricious.utility;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameContext {

    private static GameContext instance;

    public final SpriteBatch batch;
    public final FitViewport viewport;
    public final FitViewport uiViewport;
    public final DeviceInfo deviceInfo;

    private GameContext(SpriteBatch batch, FitViewport viewport, FitViewport uiViewport, DeviceInfo deviceInfo) {
        this.batch = batch;
        this.viewport = viewport;
        this.uiViewport = uiViewport;
        this.deviceInfo = deviceInfo;
    }

    public static void init(SpriteBatch batch, FitViewport viewport, FitViewport uiViewport, DeviceInfo deviceInfo) {
        if (instance != null) {
            throw new IllegalStateException("GameContext already initialized");
        }

        instance = new GameContext(batch, viewport, uiViewport, deviceInfo);
    }

    public static GameContext I() {
        if (instance == null) {
            throw new IllegalStateException("GameContext not initialized");
        }

        return instance;
    }
}
