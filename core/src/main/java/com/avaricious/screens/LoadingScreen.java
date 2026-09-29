package com.avaricious.screens;

import com.avaricious.utility.Assets;
import com.badlogic.gdx.ScreenAdapter;

public class LoadingScreen extends ScreenAdapter {

    private boolean switched = false;

    public LoadingScreen() {
        Assets.I().queueLoading();
    }

    @Override
    public void render(float delta) {
        if (Assets.I().update() && !switched) {
            switched = true;
            ScreenManager.I().setScreen(MainScreen.class);
            return;
        }

        float progress = Assets.I().getProgress();
    }
}
