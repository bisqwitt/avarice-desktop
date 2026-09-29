package com.avaricious.app.navigation;

import com.avaricious.Main;
import com.avaricious.app.AppServices;
import com.avaricious.screens.InQueueScreen;
import com.avaricious.screens.LoadingScreen;
import com.avaricious.screens.MainScreen;
import com.avaricious.screens.SlotScreen;
import com.badlogic.gdx.ScreenAdapter;

import java.util.HashMap;
import java.util.Map;

public class ScreenManager {

    private final Main app;
    private final AppServices services;

    private final Map<Class<? extends ScreenAdapter>, ScreenAdapter> screens = new HashMap<>();

    public ScreenManager(Main app, AppServices services) {
        this.app = app;
        this.services = services;
        screens.put(LoadingScreen.class, new LoadingScreen(this));
    }

    public void setScreen(Class<? extends ScreenAdapter> screenClass) {
        if (!screens.containsKey(screenClass)) {
            screens.put(screenClass, createScreen(screenClass));
        }

        app.setScreen(screens.get(screenClass));
    }

    private ScreenAdapter createScreen(
        Class<? extends ScreenAdapter> screenClass
    ) {
        if (screenClass == MainScreen.class) {
            return new MainScreen(app, services, this);
        }
        if (screenClass == SlotScreen.class) {
            return new SlotScreen(app, services);
        }
        if (screenClass == InQueueScreen.class) {
            return new InQueueScreen(app);
        }
        if (screenClass == LoadingScreen.class) {
            return new LoadingScreen(this);
        }

        throw new IllegalArgumentException(
            "No screen factory registered for " + screenClass.getName()
        );
    }

    public <T> T getScreen(Class<T> screenClass) {
        return (T) screens.get(screenClass);
    }

}
