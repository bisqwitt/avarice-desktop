package com.avaricious.utility;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Software cursor driven by relative mouse movement and player sensitivity. */
public final class MouseCursor {

    private static MouseCursor instance;

    public static MouseCursor I() {
        return instance == null ? instance = new MouseCursor() : instance;
    }

    private final Vector2 screenPosition = new Vector2();
    private boolean initialized;

    private MouseCursor() {
    }

    public void capture() {
        int centerX = Gdx.graphics.getWidth() / 2;
        int centerY = Gdx.graphics.getHeight() / 2;
        Gdx.input.setCursorPosition(centerX, centerY);
        Gdx.input.setCursorCatched(true);
        screenPosition.set(centerX, centerY);
        initialized = false;
    }

    public Vector2 update(Viewport viewport, Vector2 target) {
        if (!initialized) {
            screenPosition.set(Gdx.input.getX(), Gdx.input.getY());
            initialized = true;
        } else {
            float sensitivity = GameSettings.I().getMouseSensitivity();
            screenPosition.add(
                Gdx.input.getDeltaX() * sensitivity,
                Gdx.input.getDeltaY() * sensitivity
            );
        }

        float minX = viewport.getScreenX();
        float maxX = minX + viewport.getScreenWidth() - 1f;
        float minY = Gdx.graphics.getHeight()
            - viewport.getScreenY()
            - viewport.getScreenHeight();
        float maxY = Gdx.graphics.getHeight() - viewport.getScreenY() - 1f;
        screenPosition.set(
            MathUtils.clamp(screenPosition.x, minX, maxX),
            MathUtils.clamp(screenPosition.y, minY, maxY)
        );

        target.set(screenPosition);
        viewport.unproject(target);
        return target;
    }
}
