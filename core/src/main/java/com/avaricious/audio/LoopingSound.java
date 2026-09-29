package com.avaricious.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

public class LoopingSound {

    private final Sound start;
    private final Sound loop;
    private final Sound end;

    private final float baseVolume;
    private final float pitch;

    private long loopId = -1;
    private boolean active = false;
    private float volumeMultiplier = 1f;

    public LoopingSound(String startPath, String loopPath, String endPath, float volume, float pitch) {
        start = Gdx.audio.newSound(Gdx.files.internal("audio/" + startPath));
        loop  = Gdx.audio.newSound(Gdx.files.internal("audio/" + loopPath));
        end   = Gdx.audio.newSound(Gdx.files.internal("audio/" + endPath));
        this.baseVolume = volume;
        this.pitch = pitch;
    }

    public void start() {
        if (active) return;
        active = true;

        start.play(currentVolume(), pitch, 0f);

        loopId = loop.play(currentVolume(), pitch, 0f);
        loop.setLooping(loopId, true);
    }

    public void stop() {
        stop(true);
    }

    public void stop(boolean playEnd) {
        if (!active) return;
        active = false;

        if (loopId != -1) {
            loop.stop(loopId);
            loopId = -1;
        }

        if (playEnd && currentVolume() > 0f) {
            end.play(currentVolume(), pitch, 0f);
        }
    }

    public void setVolumeMultiplier(float multiplier) {
        volumeMultiplier = MathUtils.clamp(multiplier, 0f, 1f);
        if (loopId != -1) {
            loop.setVolume(loopId, currentVolume());
        }
    }

    private float currentVolume() {
        return baseVolume * volumeMultiplier;
    }
}
