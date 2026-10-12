package com.avaricious;

import com.avaricious.app.AppServices;
import com.avaricious.app.GameContext;
import com.avaricious.app.navigation.ScreenManager;
import com.avaricious.audio.AudioManager;
import com.avaricious.components.CompChipBar;
import com.avaricious.components.roundInfoPanel.ScoreDisplay;
import com.avaricious.components.slot.BouncingSymbolManager;
import com.avaricious.screens.LoadingScreen;
import com.avaricious.utility.Assets;
import com.avaricious.utility.DeviceInfo;
import com.avaricious.utility.GameSettings;
import com.avaricious.utility.MouseCursor;
import com.avaricious.utility.SeededRandomizer;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends Game {

    private SpriteBatch batch;
    private FitViewport viewport;
    private FitViewport uiViewport;
    private DeviceInfo deviceInfo;
    private AppServices services;
    private ScreenManager screens;
    private Cursor emptyCursor;
    private boolean disposed;

    public Main(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public Main() {
    }

    @Override
    public void create() {
        // Load player preferences before any audio, input, or UI singleton.
        GameSettings.I();
        SeededRandomizer.setSeed(MathUtils.random(1000, 9999));

        batch = new SpriteBatch();

        viewport = new FitViewport(16, 9);
        uiViewport = new FitViewport(1920, 1080);

        services = new AppServices();
        ScoreDisplay.configure(
            services.gameSession().cash(),
            services.gameSession().roundStats()
        );
        CompChipBar.configure(services.gameSession().chips());
        BouncingSymbolManager.configure(
            services.gameSession().roundStats(),
            services.gameSession().runManager().getRoundsManager().getRoundTimer(),
            services.gameSession().skillTree(),
            services.gameSession().collectibleStreak()
        );

        GameContext.init(
            batch,
            viewport,
            uiViewport,
            deviceInfo
        );

        screens = new ScreenManager(this, services);
        screens.setScreen(LoadingScreen.class);

        Pixmap pixmap =
            new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        emptyCursor = Gdx.graphics.newCursor(pixmap, 0, 0);

        Gdx.graphics.setCursor(emptyCursor);
        MouseCursor.I().capture();

        pixmap.dispose();
    }

    @Override
    public void resize(int width, int height) {

        viewport.update(width, height, true);
        uiViewport.update(width, height, true);

        // VERY IMPORTANT
        super.resize(width, height);
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
    public void pause() {
        GameSettings.I().flush();
        super.pause();
    }

    @Override
    public void dispose() {
        if (disposed) return;
        disposed = true;

        GameSettings.I().flush();
        if (services != null) {
            services.runSaves().saveNow();
        }

        if (screens != null) {
            screens.dispose();
        }
        super.dispose();

        if (emptyCursor != null) {
            emptyCursor.dispose();
        }
        AudioManager.disposeInstance();
        Assets.disposeInstance();
        if (batch != null) {
            batch.dispose();
        }
    }
}
