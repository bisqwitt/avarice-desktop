package com.avaricious.utility;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;

/** Persistent player-facing settings shared by every screen. */
public final class GameSettings {

    public static final float MIN_MOUSE_SENSITIVITY = 0.50f;
    public static final float MAX_MOUSE_SENSITIVITY = 2.00f;

    private static final String PREFERENCES_NAME = "avarice-settings";
    private static final String MOUSE_SENSITIVITY = "mouseSensitivity";
    private static final String MASTER_VOLUME = "masterVolume";
    private static final String EFFECTS_MUTED = "effectsMuted";
    private static final String MUSIC_MUTED = "musicMuted";
    private static final String SHOW_FPS = "showFps";

    private static GameSettings instance;

    public static GameSettings I() {
        return instance == null ? instance = new GameSettings() : instance;
    }

    private final Preferences preferences;
    private float mouseSensitivity;
    private float masterVolume;
    private boolean effectsMuted;
    private boolean musicMuted;
    private boolean showFps;
    private boolean dirty;

    private GameSettings() {
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        mouseSensitivity = MathUtils.clamp(
            preferences.getFloat(MOUSE_SENSITIVITY, 1f),
            MIN_MOUSE_SENSITIVITY,
            MAX_MOUSE_SENSITIVITY
        );
        masterVolume = MathUtils.clamp(
            preferences.getFloat(MASTER_VOLUME, 1f),
            0f,
            1f
        );
        effectsMuted = preferences.getBoolean(EFFECTS_MUTED, false);
        musicMuted = preferences.getBoolean(MUSIC_MUTED, false);
        showFps = preferences.getBoolean(SHOW_FPS, false);
    }

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public void setMouseSensitivity(float value) {
        float clamped = MathUtils.clamp(
            value,
            MIN_MOUSE_SENSITIVITY,
            MAX_MOUSE_SENSITIVITY
        );
        if (MathUtils.isEqual(mouseSensitivity, clamped, 0.001f)) return;
        mouseSensitivity = clamped;
        dirty = true;
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float value) {
        float clamped = MathUtils.clamp(value, 0f, 1f);
        if (MathUtils.isEqual(masterVolume, clamped, 0.001f)) return;
        masterVolume = clamped;
        dirty = true;
    }

    public boolean isEffectsMuted() {
        return effectsMuted;
    }

    public void setEffectsMuted(boolean muted) {
        if (effectsMuted == muted) return;
        effectsMuted = muted;
        dirty = true;
    }

    public boolean isMusicMuted() {
        return musicMuted;
    }

    public void setMusicMuted(boolean muted) {
        if (musicMuted == muted) return;
        musicMuted = muted;
        dirty = true;
    }

    public boolean isFpsVisible() {
        return showFps;
    }

    public void setFpsVisible(boolean visible) {
        if (showFps == visible) return;
        showFps = visible;
        dirty = true;
    }

    public void flush() {
        if (!dirty) return;

        preferences
            .putFloat(MOUSE_SENSITIVITY, mouseSensitivity)
            .putFloat(MASTER_VOLUME, masterVolume)
            .putBoolean(EFFECTS_MUTED, effectsMuted)
            .putBoolean(MUSIC_MUTED, musicMuted)
            .putBoolean(SHOW_FPS, showFps)
            .flush();
        dirty = false;
    }
}
