package com.avaricious.app.navigation;

import com.avaricious.Main;
import com.avaricious.app.AppServices;
import com.avaricious.screens.InQueueScreen;
import com.avaricious.screens.LoadingScreen;
import com.avaricious.screens.MainScreen;
import com.avaricious.screens.SlotScreen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Creates, caches, and owns the application's screens. */
public final class ScreenManager implements Disposable {

    private final Main app;
    private final AppServices services;
    private final Map<Class<? extends ScreenAdapter>, ScreenAdapter> screens =
        new HashMap<>();
    private boolean disposed;

    public ScreenManager(Main app, AppServices services) {
        this.app = Objects.requireNonNull(app, "app");
        this.services = Objects.requireNonNull(services, "services");
        screens.put(LoadingScreen.class, new LoadingScreen(this));
    }

    public void setScreen(Class<? extends ScreenAdapter> screenClass) {
        ensureActive();
        app.setScreen(getOrCreate(screenClass));
    }

    private ScreenAdapter getOrCreate(
        Class<? extends ScreenAdapter> screenClass
    ) {
        Objects.requireNonNull(screenClass, "screenClass");
        ScreenAdapter screen = screens.get(screenClass);
        if (screen == null) {
            screen = createScreen(screenClass);
            screens.put(screenClass, screen);
        }
        return screen;
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

    public <T extends ScreenAdapter> T getScreen(Class<T> screenClass) {
        ensureActive();
        return screenClass.cast(getOrCreate(screenClass));
    }

    @Override
    public void dispose() {
        if (disposed) return;

        disposed = true;
        app.setScreen(null);
        for (ScreenAdapter screen : screens.values()) {
            screen.dispose();
        }
        screens.clear();
    }

    private void ensureActive() {
        if (disposed) {
            throw new IllegalStateException("ScreenManager is disposed");
        }
    }

}
