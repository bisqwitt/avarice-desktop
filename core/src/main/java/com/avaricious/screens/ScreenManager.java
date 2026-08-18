package com.avaricious.screens;

import com.avaricious.Main;
import com.badlogic.gdx.ScreenAdapter;

import java.util.HashMap;
import java.util.Map;

public class ScreenManager {

    private static ScreenManager instance;

    public static ScreenManager create(Main app) {
        return (instance = new ScreenManager(app));
    }

    public static ScreenManager I() {
        return instance;
    }

    private final Main app;

    private final Map<Class<? extends ScreenAdapter>, ScreenAdapter> screens = new HashMap<>();

    private ScreenManager(Main app) {
        this.app = app;
        screens.put(LoadingScreen.class, new LoadingScreen());
    }

    public void setScreen(Class<? extends ScreenAdapter> screenClass) {
        try {
            if (!screens.containsKey(screenClass)) {
                ScreenAdapter screen = screenClass
                    .getConstructor(Main.class)
                    .newInstance(app);

                screens.put(screenClass, screen);
            }

            app.setScreen(screens.get(screenClass));

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not create screen: " + screenClass.getName(), e);
        }
    }

    public <T> T getScreen(Class<T> screenClass) {
        return (T) screens.get(screenClass);
    }

}
