package com.avaricious.screens;

import com.avaricious.app.navigation.ScreenManager;
import com.avaricious.utility.Assets;
import com.badlogic.gdx.ScreenAdapter;

public class LoadingScreen extends ScreenAdapter {

    private final ScreenManager screens;
    private boolean switched = false;

    public LoadingScreen(ScreenManager screens) {
        this.screens = screens;
        Assets.I().queueLoading();
    }

    @Override
    public void render(float delta) {
        if (Assets.I().update() && !switched) {
            switched = true;
            screens.setScreen(MainScreen.class);
            return;
        }

        float progress = Assets.I().getProgress();
    }
}
